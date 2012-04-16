package org.safecast.ui.qa;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.safecast.qa.QaBackend;
import org.safecast.qa.QaTaskListener;
import org.safecast.task.HasProgress;
import org.safecast.ui.log.LogPanel;
import org.safecast.ui.task.ProgressDialog;

@SuppressWarnings("serial")
public class QaGui extends JFrame implements QaTaskListener
{
  public static final String APP_VERSION = getAppVersion();
  public static final ResourceBundle STRINGS = loadLocalisationData();
  
  public static final String WINDOW_TITLE = QaGui.STRINGS.getString("window_title");
  public static final int WINDOW_WIDTH = 1024;
  public static final int WINDOW_HEIGHT = 768;
  public static final int LOG_PANEL_SIZE = 100;

  private ProgressDialog progress;
  private QaBackend backend;
  private JTabbedPane tabs;
  private LogPanel logPanel;
  private QaPanel qaPanel;
  private String currentFile;

  private static String getAppVersion()
  {
    String appVersion = "?";
    InputStream in = QaGui.class.getClassLoader().getResourceAsStream("build.properties");
    if (in != null)
    {
      Properties props = new java.util.Properties();
      try
      {
        props.load(in);
        appVersion = props.getProperty("version");
      }
      catch(IOException e) {}
    }
    return appVersion;
  }
  
  public QaGui()
  {
    setupUi();
    displayOpenFileChooser();
  }

  public QaGui(String inputFile)
  {
    setupUi();
    displayFile(inputFile);
  }

  private void setupUi()
  {
    setTitle(WINDOW_TITLE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    qaPanel = new QaPanel();
    logPanel = new LogPanel(LOG_PANEL_SIZE);
    setLayout(new BorderLayout());
    createTabs(qaPanel, logPanel);
    progress = new ProgressDialog(this);
    createMenuAndToolbar();
  }

  private void createMenuAndToolbar()
  {
    JPanel panel = new JPanel(new BorderLayout());
    JToolBar toolbar = new JToolBar();   
    JMenuBar menuBar = new JMenuBar();
    populateMenuAndToolbar(menuBar, toolbar);
    panel.add(BorderLayout.CENTER, toolbar);
    panel.add(BorderLayout.NORTH, menuBar);
    add(BorderLayout.NORTH, panel);    
  }
  
  private void populateMenuAndToolbar(JMenuBar menuBar, JToolBar toolbar)
  {    
    Action open = new OpenAction();
    Action reload = new ReloadAction();
    Action about = new AboutAction();

    JMenu fileMenu = new JMenu(QaGui.STRINGS.getString("file"));
    fileMenu.add(open);
    fileMenu.add(reload);
    JMenu helpMenu = new JMenu(QaGui.STRINGS.getString("help"));
    helpMenu.add(about);
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);
    
    toolbar.add(open);
    toolbar.add(reload);
    toolbar.add(about);
  }

  private void reload()
  {
    if (null != currentFile)
      displayFile(currentFile);
  }

  private void displayFile(final String inputFile)
  {
    currentFile = inputFile;
    backend = new QaBackend(inputFile);
    backend.getLogger().setLogListener(logPanel);
    backend.start(this, qaPanel);
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        setTitle(WINDOW_TITLE + " - " + inputFile);
      }
    });
  }

  private void displayOpenFileChooser()
  {
    final JFileChooser fc = new JFileChooser();
    if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(QaGui.this))
    {
      displayFile(fc.getSelectedFile().getAbsolutePath());
    }
  }

  private void createTabs(JPanel qaPanel, JPanel logPanel)
  {
    tabs = new JTabbedPane();
    tabs.setTabPlacement(JTabbedPane.BOTTOM);
    tabs.addTab(QaGui.STRINGS.getString("display"), qaPanel);
    tabs.addTab(QaGui.STRINGS.getString("log"), logPanel);
    add(BorderLayout.CENTER, tabs);
  }

  private class ReloadAction extends AbstractAction
  {
    public ReloadAction()
    {
      super(QaGui.STRINGS.getString("reload"), UIManager.getIcon("FileChooser.upFolderIcon"));
      putValue(SHORT_DESCRIPTION, QaGui.STRINGS.getString("reload"));
      putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    }

    public void actionPerformed(ActionEvent e)
    {
      reload();
    }
  }

  private class OpenAction extends AbstractAction
  {
    public OpenAction()
    {
      super(QaGui.STRINGS.getString("open"), UIManager.getIcon("Tree.openIcon"));
      putValue(SHORT_DESCRIPTION, QaGui.STRINGS.getString("open"));
      putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      displayOpenFileChooser();
    }
  }

  private class AboutAction extends AbstractAction
  {
    public AboutAction()
    {
      super(QaGui.STRINGS.getString("about"), UIManager.getIcon("FileView.fileIcon"));
      putValue(SHORT_DESCRIPTION, QaGui.STRINGS.getString("about"));
      putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    }

    public void actionPerformed(ActionEvent e)
    {
      JOptionPane.showMessageDialog(QaGui.this,
          QaGui.STRINGS.getString("version") + APP_VERSION,
          QaGui.STRINGS.getString("about"),
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  
  public static void main(final String[] args)
  {
    loadLocalisationData();
    setLookAndFeel();

    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        QaGui gui;
        if (args.length > 0)
          gui = new QaGui(args[0]);
        else
          gui = new QaGui();
        gui.setDefaultCloseOperation(EXIT_ON_CLOSE);
        gui.setVisible(true);
      }
    });
  }

  public void taskStarted(HasProgress task)
  {
    progress.taskStarted(task, backend);
  }

  public void taskCompleted()
  {
    progress.taskCompleted();
  }

  public void taskError(String error)
  {
    progress.taskCompleted();
    backend.getLogger().logError(error);
  }
 
  private static ResourceBundle loadLocalisationData()
  {
    ResourceBundle r;
    try
    {
      r = ResourceBundle.getBundle("strings");
    }
    catch (Exception e)
    {
      r = ResourceBundle.getBundle("strings", new Locale("en", "AU"));
    }
    return r;
  }

  private static void setLookAndFeel()
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e)
    {
      // oh well, live with the default cross platform look and feel
    }
  }
  
}
