package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PlaceholderPanel extends JPanel
{
  public PlaceholderPanel(String placeholderText)
  {
    super();
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setLayout(new BorderLayout());
    JLabel label = new JLabel(placeholderText, JLabel.CENTER);
    add(BorderLayout.CENTER, label);
  }
}
