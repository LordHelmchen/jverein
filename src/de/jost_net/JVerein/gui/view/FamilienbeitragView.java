/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 * heiner@jverein.de
 * www.jverein.de
 * $Log$
 * Revision 1.2  2011-08-01 18:27:57  jost
 * Funktionalität aus FamilienbeitragControl in MitgliedControl verschoben.
 *
 * Revision 1.1  2011-07-24 18:04:27  jost
 * Neu: Auflistung Familienbeiträge
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;

public class FamilienbeitragView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(JVereinPlugin.getI18n().tr("Familienbeitrag"));

    final MitgliedControl control = new MitgliedControl(this);

    control.getFamilienbeitraegeTree().paint(this.getParent());

    ButtonArea buttons = new ButtonArea(getParent(), 2);
    buttons.addButton(JVereinPlugin.getI18n().tr("Hilfe"),
        new DokumentationAction(), DokumentationUtil.ANFANGSBESTAENDE, false,
        "help-browser.png");
  }

  @Override
  public String getHelp()
  {
    return "<form><p><span color=\"header\" font=\"header\">Familienbeiträge</span></p>"
    +"<p>Doppelklick öffnet das Mitglied. Rechtsklick bietet die Möglichkeit, das Mitglied aus dem Famlilienverband zu entfernen.</p>"
        + "</form>";
  }
}
