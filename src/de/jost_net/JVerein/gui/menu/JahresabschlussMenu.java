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
 **********************************************************************/
package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.JahresabschlussDeleteAction;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Jahresabschl�ssen.
 */
public class JahresabschlussMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Jahresabschl�sse
   */
  public JahresabschlussMenu()
  {
    addItem(new CheckedContextMenuItem("L�schen...",
        new JahresabschlussDeleteAction()));
  }
}