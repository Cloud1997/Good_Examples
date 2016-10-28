package com.zf;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * A general FilterComboBox used in JFrame.
 * @author zhanhfan
 *
 */
public class FilterComboBox extends JPanel
{

  private String             lastValidValue;
  // this is the data list for filtering
  private String[]           data;
  //singleton pop window, when using it, it must be associated with the correct instance of FilterComboBox.
  private static PopupWindow pop;
  private JTextComponent     textField;
  private AbstractButton     button;
  private static boolean     isUserSelection;
  private boolean            updating;
  private List               inputListeners;
  private boolean            isReplacing;

  public static void main(String[] args)
  {
    String[] patternExamples = { "111", "222", "333", "4444", "55555", "666666", "777777777", "8888888888",
        "99999998877", "123", "234", "3434", "999999999999", "123", "234", "3434", "aabb" };
    JFrame f = new JFrame("test");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel pane = new JPanel();
    pane.add(new FilterComboBox(patternExamples, "999999999999"));
    pane.add(new JTextField("Another"));
    pane.add(new JButton("haha"));
    f.getContentPane().add(pane);
    f.pack();
    f.setVisible(true);
  }

  @Override
  public void setForeground(Color fg)
  {
    super.setForeground(fg);
    //the internal text field should be covered.
    if (textField != null)
    {
      textField.setForeground(fg);

    }
  }

  /**
   * 
   * @param data, the data list.
   * @param seleceted, this must matched with one of the data.
   */
  public FilterComboBox(String[] data, String seleceted)
  {
    super(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

    textField = new JTextField();
    //    textField = new FilterTextField(seleceted);
    //when replace action happen, only need to update the pop in insertUpdate();
    textField.setDocument(new PlainDocument() {
      @Override
      public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException
      {
        isReplacing = true;
        try
        {
          super.replace(offset, length, text, attrs);
        }
        finally
        {
          isReplacing = false;
        }
      }
    });
    textField.setText(seleceted);
    textField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));

    //    button = new FilterButton();
    button = new BasicArrowButton(SwingConstants.SOUTH);
    button.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
    button.setPreferredSize(new Dimension(20, 20));
    add(textField, gc);
    gc.gridx = 1;
    gc.weightx = 0.0;
    add(button, gc);
    this.data = new String[data.length];
    System.arraycopy(data, 0, this.data, 0, data.length);
    lastValidValue = seleceted;
    init();
  }

  public void updateAllData(String[] data, String input)
  {
    //update the datas in FilterComboBox
    //when updating don't trigger event on text field.
    updating = true;
    this.data = data;
    textField.setText(input);
    pop.updateData(data);
    updating = false;
  }

  public String[] getAllData()
  {
    return this.data;
  }

  public String getInput()
  {
    return textField.getText();
  }

  public String getLastValidValue()
  {
    return lastValidValue;

  }

  public boolean isInputValid()
  {
    String input = textField.getText();
    for (String value : data)
    {
      if (input.equalsIgnoreCase(value))
      {
        return true;
      }
    }
    return false;
  }

  private void showPop()
  {
    //get the location of combo box when adjust the pop location.
    Point p = getLocationOnScreen();
    pop.setLocation(p.x, p.y + getHeight());
    pop.setVisible(true);
  }

  @Override
  public void setVisible(boolean aFlag)
  {
    super.setVisible(aFlag);
    if (!aFlag)
    {
      pop.setVisible(false);
    }
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    textField.setEnabled(enabled);
    button.setEnabled(enabled);

  }

  private static synchronized void prepareWindow(FilterComboBox comboBox)
  {
    if (pop == null)
    {
      //this JWindow's father is a temporary JFrame, only used to get FOCUS.
      JFrame tempFather = new JFrame();
      tempFather.setUndecorated(true);
      tempFather.setType(Window.Type.UTILITY);
      tempFather.setVisible(true);
      pop = new PopupWindow(tempFather);
    }
    //associate popup window with the working FilterComboBox.
    pop.linkTo(comboBox);
  }

  private void init()
  {

    button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        //associate popup window with the working FilterComboBox, lazily
        prepareWindow(FilterComboBox.this);
        //when button is clicked, hide pop if it is visible
        if (pop.isVisible())
        {
          pop.setVisible(false);
        }
        else
        {
          //when pop is invisible, show it with the full data list.
          pop.undoFilt();
          //          showPop();
        }
      }
    });

    textField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        //don't do anything when it is replace action from user
        if (isReplacing)
        {
          return;
        }
        //associate popup window with the working FilterComboBox, lazily
        prepareWindow(FilterComboBox.this);
        if (!isUserSelection && !updating)
        {
          fireUpdate(e);
        }
      }

      @Override
      public void insertUpdate(DocumentEvent e)
      {
        //associate popup window with the working FilterComboBox, lazily
        prepareWindow(FilterComboBox.this);
        if (!isUserSelection && !updating)
        {
          fireUpdate(e);
        }

      }

      private void fireUpdate(DocumentEvent e)
      {
        try
        {
          String text = e.getDocument().getText(0, e.getDocument().getLength());
          //when user is typing in the text field, dynamically filter the result in popup.

          pop.filtList(text);
          //notify all of the inputListeners
          if (inputListeners != null)
          {
            Iterator it = inputListeners.iterator();
            while (it.hasNext())
            {
              InputListener il = (InputListener)it.next();
              il.inputChanged(new InputEvent(FilterComboBox.this));
            }
          }
        }
        catch (Exception ex)
        {
          //do nothing
        }
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        //do nothing
      }
    });

  }

  public void addInputListener(InputListener listener)
  {
    if (inputListeners == null)
    {
      inputListeners = new ArrayList();
    }
    inputListeners.add(listener);
  }

  private static class PopupWindow extends JWindow
  {
    private int            selIndex;
    private JLabel[]       items;
    private String[]       data;
    private JScrollPane    scrollPane;
    private JPanel         popupPanel;
    private int            orig_width;
    private int            orig_height;
    private FilterComboBox combo;

    PopupWindow(Frame father)
    {
      super(father);
      setFocusable(true);
      setAutoRequestFocus(true);
      Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

        @Override
        public void eventDispatched(AWTEvent event)
        {
          //when user scroll mouse wheel out side of pop or there is no scroll bar, hide it.
          if (pop.isVisible())
          {
            if (event instanceof MouseWheelEvent)
            {
              if (pop != SwingUtilities.getWindowAncestor((Component)event.getSource()))
              {
                pop.setVisible(false);
              }
            }
          }
        }

      }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
      //pop will have focus when it is visible, you need to dispatch KeyEvent to the textfield to update the text.
      addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e)
        {
          handleKeyEvent(e);
        }

        private void handleKeyEvent(KeyEvent e)
        {
          int kc = e.getKeyCode();
          boolean keyReleasd = e.getID() == KeyEvent.KEY_RELEASED;
          switch (kc)
          {
            //press down to select the lower one in list.
            case KeyEvent.VK_DOWN:
              if (keyReleasd)
              {
                int temp = selIndex;
                undoLastSelect();
                selIndex = temp;
                selectDown();
              }
              break;
            // press enter to trigger fireValueSelected and update the textfield, just the same as mouse click on the selected one.
            case KeyEvent.VK_ENTER:
              if (selIndex > -1 && selIndex < items.length)
              {
                ColorStyleLabel label = (ColorStyleLabel)items[selIndex];
                fireValueSelected(label.getValue());
                setVisible(false);
              }
              break;
            // press up to select the upper one in list.
            case KeyEvent.VK_UP:
              if (keyReleasd)
              {
                int temp = selIndex;
                undoLastSelect();
                selIndex = temp;
                selectUp();
              }
              break;
            //undo select if user input other character, and dispatch the key event to textfield
            default:
              undoLastSelect();
              combo.textField.dispatchEvent(e);

          }
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
          handleKeyEvent(e);
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
          handleKeyEvent(e);
        }
      });
      addFocusListener(new FocusListener() {

        @Override
        public void focusLost(FocusEvent e)
        {
          //this is a trick, the purpose is want to detect when pop lost focus because of mouse click, check if mouse checked on the textfield or button
          Component com = e.getOppositeComponent();
          if (com != null)
          {
            Point p = MouseInfo.getPointerInfo().getLocation();
            Point temp1 = new Point(p);
            //            Point temp2 = new Point(p);
            SwingUtilities.convertPointFromScreen(temp1, combo.button);
            //            SwingUtilities.convertPointFromScreen(temp2, combo.textField);
            if (combo.button.contains(temp1))
            {
            }
            //            else if (combo.textField.contains(temp2))
            //            {
            //              
            //            }
            else
            {
              pop.setVisible(false);
            }
          }
          else
          {
            //if the application lost focus.
            pop.setVisible(false);
          }

        }

        @Override
        public void focusGained(FocusEvent e)
        {
          //when pop get focus from textfield, you must let the caret flash since you will dispatch key event to the textfield.
          if (e.getOppositeComponent() == combo.textField)
          {
            combo.textField.getCaret().setVisible(true);
          }

        }
      });

    }

    private void selectUp()
    {
      int index = this.selIndex;
      if (index < -1 || index > items.length)
      {
        return;
      }
      //find the first visible JLabel and update its color, update selIndex as well.
      int loopCount = 0;
      do
      {
        //avoid dead loop;
        if (loopCount++ > items.length)
        {
          break;
        }
        if (index > 0)
        {
          index--;
          if (items[index].isVisible())
          {
            select(index);
            break;
          }
          else
          {
            continue;
          }
        }
        else if (index == 0 || index == -1)
        {
          index = items.length;
          continue;
        }
      }
      while (true);

    }

    private void select(int index)
    {
      if (index < 0 || index > items.length - 1)
      {
        return;
      }
      items[index].setBackground(Color.BLUE);
      items[index].setForeground(Color.WHITE);
      selIndex = index;
    }

    private void selectDown()
    {
      int index = this.selIndex;
      if (index < -1 || index > items.length - 1)
      {
        return;
      }
      int loopCount = 0;
      do
      {
        //avoid dead loop;
        if (loopCount++ > items.length)
        {
          break;
        }
        //find the first visible JLabel and update its color, update selIndex as well.
        if (index < items.length - 1)
        {
          index++;
          if (items[index].isVisible())
          {
            select(index);
            break;
          }
          else
          {
            continue;
          }

        }
        else if (index == items.length - 1)
        {
          index = -1;
          continue;
        }
      }
      while (true);

    }

    private void undoLastSelect()
    {
      if (selIndex != -1 && selIndex != items.length)
      {
        items[selIndex].setBackground(Color.WHITE);
        items[selIndex].setForeground(Color.BLACK);
      }
      selIndex = -1;
    }

    private void linkTo(FilterComboBox combo)
    {

      //if the instance is not the same, then pop content should be updated.
      if (this.combo != combo)
      {
        this.combo = combo;
        pop.updateData(combo.data);
      }
    }

    private void fireValueSelected(String value)
    {
      //when user select one of the value in the list, don't trigger updating of the list.
      isUserSelection = true;
      combo.textField.setText(value);
      combo.lastValidValue = value;
      isUserSelection = false;
      if (combo.inputListeners != null)
      {
        Iterator it = combo.inputListeners.iterator();
        while (it.hasNext())
        {
          InputListener il = (InputListener)it.next();
          il.inputChanged(new InputEvent(combo));
        }
      }
    }

    /**
     * update the data list in the popup window
     * @param data
     */
    private void updateData(String[] data)
    {
      if (data == null || data.length == 0)
      {
        return;
      }
      GridBagConstraints gc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
      this.data = new String[data.length];
      this.items = new ColorStyleLabel[data.length];
      System.arraycopy(data, 0, this.data, 0, data.length);
      Container root = getContentPane();
      //remove all and re-add new labels with new data.
      root.removeAll();
      popupPanel = new JPanel(new GridBagLayout());

      for (int i = 0; i < data.length; i++)
      {
        final int index = i;
        items[i] = new ColorStyleLabel(this.data[i]);
        items[i].setOpaque(true);
        items[i].setBackground(Color.WHITE);
        items[i].setForeground(Color.BLACK);
        items[i].addMouseListener(new MouseAdapter() {
          private final int itemIndex = index;

          @Override
          public void mouseClicked(MouseEvent e)
          {
            //click on a label means the user want to select this item.
            ColorStyleLabel label = (ColorStyleLabel)e.getComponent();
            fireValueSelected(label.getValue());
            PopupWindow.this.setVisible(false);
          }

          @Override
          public void mouseEntered(MouseEvent e)
          {
            //un select the last first
            undoLastSelect();
            //update the color to hightlight the current one which is hovered on.
            //also update the selIndex to the current.
            selIndex = itemIndex;
            select(selIndex);
          }

          @Override
          public void mouseExited(MouseEvent e)
          {
            //un select the last
            undoLastSelect();
          }

        });
        popupPanel.add(items[i], gc);
        gc.gridy = gc.gridy + 1;
      }
      scrollPane = new JScrollPane(popupPanel);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      root.add(scrollPane);
      Dimension d = popupPanel.getPreferredSize();
      //calculate and set the size of the label,this will impact the scroll pane
      for (JLabel label : items)
      {
        label.setPreferredSize(new Dimension(d.width, label.getPreferredSize().height));
      }
      //calculate and set the view size of the scroll pane
      orig_width = d.width + 20;
      orig_width = orig_width < 150 ? 150 : orig_width;
      orig_height = d.height > 200 ? 200 : d.height + 3;
      scrollPane.setPreferredSize(new Dimension(orig_width, orig_height));
    }

    @Override
    public void setVisible(boolean v)
    {
      //when set to not visible, must let combo.textField get focus first, to avoid focus lost after setVisible.
      if (!v)
      {
        //undo selection so the next time user see the pop, there is no selection(this is correct)
        undoLastSelect();
        if (combo != null)
        {
          combo.textField.requestFocus();
        }
      }
      super.setVisible(v);
      if (v)
      {
        requestFocus();
      }
    }

    /**
     * filter the JLabel list based on the given String condition.
     * @param value
     */
    private void filtList(final String value)
    {
      if (value.length() == 0)
      {
        undoFilt();
        return;
      }
      //space is treated as AND condition
      String[] conditions = value.split(" ");
      for (int i = 0; i < data.length; i++)
      {
        String result = renderMatchResult(data[i], conditions);
        //hide unmatched results
        if (result == null)
        {
          items[i].setVisible(false);
        }
        else
        {
          items[i].setText(result);
          items[i].setVisible(true);
        }
      }
      //you need to resize the scroll pane since it could be smaller.
      Dimension d = popupPanel.getPreferredSize();
      int height = d.height > 200 ? 200 : d.height;
      if (height == 0)
      {
        pop.setVisible(false);
        return;
      }
      scrollPane.setPreferredSize(new Dimension(combo.getSize().width, height + 3));
      pop.pack();
      if (!pop.isVisible())
      {
        combo.showPop();
      }
    }

    /**
     * find out if the conditions matches the source.
     * @param source
     * @param conditions
     * @return the html rendered result to hightlight the match result.
     */
    private String renderMatchResult(String source, String[] conditions)
    {
      boolean matchFound = true;
      //only use the first 3 conditions, for performance.
      int[][] matches = new int[conditions.length > 3 ? 3 : conditions.length][2];
      for (int i = 0; i < conditions.length && i < 3; i++)
      {
        int indexStart = source.toLowerCase().indexOf(conditions[i].toLowerCase());
        if (indexStart != -1)
        {
          matches[i][0] = indexStart;
          matches[i][1] = indexStart + conditions[i].length();
        }
        else
        {
          matchFound = false;
          break;
        }
      }

      if (matchFound)
      {
        //first sort the match result, ASC
        Arrays.sort(matches, new Comparator<int[]>() {

          @Override
          public int compare(int[] array1, int[] array2)
          {
            //only need to compare the indexStart part.
            if (array1[0] > array2[0])
              return 1;
            if (array1[0] == array2[0])
              return 0;
            else
              return -1;
          }
        });
        //build up html render String for JLabel
        int index = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        String sub = "";
        for (int i = 0; i < matches.length && i < 3; i++)
        {
          if (matches[i][0] != -1)
          {
            if (index <= matches[i][0])
            {
              sub = source.substring(index, matches[i][0]);
              index = matches[i][1];

              sb.append(sub);
              sub = source.substring(matches[i][0], matches[i][1]);
            }
            else
            {
              sub = source.substring(index, matches[i][1]);
              index = matches[i][1];
            }
            sb.append("<span style='color:#1aac1d;font-weight:bold'>");
            sb.append(sub);
            sb.append("</span>");
          }
        }
        sb.append(source.substring(index, source.length()));
        sb.append("</html>");

        return sb.toString();
      }
      else
      {
        return null;
      }

    }

    /**
     * list out all data
     */
    private void undoFilt()
    {
      for (JLabel label : items)
      {
        ColorStyleLabel temp = (ColorStyleLabel)label;
        temp.setText(temp.getValue());
        temp.setVisible(true);
      }

      scrollPane.setPreferredSize(new Dimension(combo.getSize().width, orig_height));
      pack();
      combo.showPop();
    }
  }

  private static class ColorStyleLabel extends JLabel
  {
    private String value;

    public ColorStyleLabel(String str)
    {
      super(str);
      this.value = str;
    }

    public String getValue()
    {
      return this.value;
    }

  }

  public interface InputListener
      extends EventListener
  {
    void inputChanged(InputEvent e);
  }

  public static class InputEvent extends EventObject
  {
    private Object source;

    public InputEvent(Object source)
    {
      super(source);
      this.source = source;
    }

    public String getInput()
    {
      FilterComboBox afc = (FilterComboBox)source;
      return afc.getInput();
    }

  }

}
