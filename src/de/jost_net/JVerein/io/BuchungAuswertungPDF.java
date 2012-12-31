/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungAuswertungPDF
{

  private double summe = 0;

  private double summeeinnahmen = 0;

  private double summeausgaben = 0;

  private double summeumbuchungen = 0;

  public BuchungAuswertungPDF(ArrayList<Buchungsart> buchungsarten,
      final File file, BuchungQuery query, boolean einzel)
      throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String title = null;
      if (einzel)
      {
        title = JVereinPlugin.getI18n().tr("Buchungsliste");
      }
      else
      {
        title = JVereinPlugin.getI18n().tr("Summenliste");
      }
      Reporter reporter = new Reporter(fos, title, query.getSubtitle(),
          buchungsarten.size());

      if (!einzel)
      {
        createTableHeaderSumme(reporter);
      }

      for (Buchungsart bua : buchungsarten)
      {
        query.setOrderDatum();
        createTableContent(reporter, bua, query.get(), einzel);
      }
      if (buchungsarten.size() > 1)
      {
        if (einzel)
        {
          createTableHeaderEinzel(reporter);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn(JVereinPlugin.getI18n().tr("Gesamtsumme"),
              Element.ALIGN_LEFT);
          reporter.addColumn(summe);
          reporter.closeTable();
        }
        else
        {
          reporter.addColumn(JVereinPlugin.getI18n().tr("Summe Einnahmen"),
              Element.ALIGN_LEFT);
          reporter.addColumn(summeeinnahmen);
          reporter.addColumn(JVereinPlugin.getI18n().tr("Summe Ausgaben"),
              Element.ALIGN_LEFT);
          reporter.addColumn(summeausgaben);
          reporter.addColumn(JVereinPlugin.getI18n().tr("Summe Umbuchungen"),
              Element.ALIGN_LEFT);
          reporter.addColumn(summeumbuchungen);
          reporter.addColumn(JVereinPlugin.getI18n().tr("Saldo"),
              Element.ALIGN_LEFT);
          reporter.addColumn(summeeinnahmen + summeausgaben + summeumbuchungen);
        }

      }
      GUI.getStatusBar().setSuccessText(
          JVereinPlugin.getI18n().tr("Auswertung fertig."));

      reporter.close();
      fos.close();
      GUI.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            new Program().handleAction(file);
          }
          catch (ApplicationException ae)
          {
            Application.getMessagingFactory().sendMessage(
                new StatusBarMessage(ae.getLocalizedMessage(),
                    StatusBarMessage.TYPE_ERROR));
          }
        }
      });
    }
    catch (DocumentException e)
    {
      e.printStackTrace();
      Logger.error("error while creating report", e);
      throw new ApplicationException(JVereinPlugin.getI18n().tr("Fehler"), e);
    }
    catch (FileNotFoundException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException(JVereinPlugin.getI18n().tr("Fehler"), e);
    }
    catch (IOException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException(JVereinPlugin.getI18n().tr("Fehler"), e);
    }
  }

  private void createTableHeaderEinzel(Reporter reporter)
      throws DocumentException
  {
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Datum"),
        Element.ALIGN_CENTER, 40, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Auszug"),
        Element.ALIGN_CENTER, 20, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Name"),
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Zahlungsgrund"),
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Betrag"),
        Element.ALIGN_CENTER, 50, BaseColor.LIGHT_GRAY);
    reporter.createHeader();

  }

  private void createTableHeaderSumme(Reporter reporter)
      throws DocumentException
  {
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Buchungsart"),
        Element.ALIGN_CENTER, 200, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn(JVereinPlugin.getI18n().tr("Betrag"),
        Element.ALIGN_CENTER, 60, BaseColor.LIGHT_GRAY);
    reporter.createHeader();
  }

  private void createTableContent(Reporter reporter, Buchungsart bua,
      ArrayList<Buchung> buchungen, boolean einzel) throws RemoteException,
      DocumentException
  {
    if (einzel)
    {
      Paragraph pBuchungsart = new Paragraph(bua.getBezeichnung(),
          FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
      reporter.add(pBuchungsart);
    }
    double buchungsartSumme = 0;
    if (einzel)
    {
      createTableHeaderEinzel(reporter);
    }
    boolean gedruckt = false;

    for (Buchung b : buchungen)
    {
      if (bua.getArt() != -1

          && (b.getBuchungsart() == null || b.getBuchungsart().getNummer() != bua
              .getNummer()))
      {
        continue;
      }

      if (bua.getArt() == -1 && b.getBuchungsart() != null)
      {
        continue;
      }

      gedruckt = true;
      if (einzel)
      {
        reporter.addColumn(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
            Element.ALIGN_LEFT);
        if (b.getAuszugsnummer() != null)
        {
          reporter.addColumn(b.getAuszugsnummer() + "/" + b.getBlattnummer(),
              Element.ALIGN_LEFT);
        }
        else
        {
          reporter.addColumn("", Element.ALIGN_LEFT);
        }
        reporter.addColumn(b.getName(), Element.ALIGN_LEFT);
        reporter.addColumn(b.getZweck(), Element.ALIGN_LEFT);
        reporter.addColumn(b.getBetrag());
      }
      buchungsartSumme += b.getBetrag();
      if (bua.getArt() == ArtBuchungsart.EINNAHME)
      {
        summeeinnahmen += b.getBetrag();
      }
      if (bua.getArt() == ArtBuchungsart.AUSGABE)
      {
        summeausgaben += b.getBetrag();
      }
      if (bua.getArt() == ArtBuchungsart.UMBUCHUNG)
      {
        summeumbuchungen += b.getBetrag();
      }
    }
    if (einzel)
    {
      if (!gedruckt)
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn(JVereinPlugin.getI18n().tr("keine Buchung"),
            Element.ALIGN_LEFT);
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn("", Element.ALIGN_LEFT);
      }
      else
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn("", Element.ALIGN_LEFT);
        reporter.addColumn(
            JVereinPlugin.getI18n().tr("Summe {0}", bua.getBezeichnung()),
            Element.ALIGN_LEFT);
        summe += buchungsartSumme;
        reporter.addColumn(buchungsartSumme);
      }
    }
    else
    {
      reporter.addColumn(bua.getBezeichnung(), Element.ALIGN_LEFT);
      reporter.addColumn(buchungsartSumme);
    }
    if (einzel)
    {
      reporter.closeTable();
    }
  }
}