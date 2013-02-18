/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ngochoang.validation;

import com.ngochoang.CrawlerLib.Utilities;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author Hoang
 */
public class UserInput {

    public static int CT_TEXTBOX = 0;
    public static int CT_COMBOBOX = 1;
    public static int CT_LISTBOX = 2;
    public static int CT_PASSWORD = 3;


    public JComponent control;
    public int maxLength = 9999999;
    public boolean isNumberOnly = false;
    public boolean isStringOnly = false;
    public boolean isRequire = false;
    public int ControlType = CT_TEXTBOX;
    public String allowChars = "";
    public String ErrorText = "Validation is failed";
    public int minLength = -1;
    public int rangeFrom = -9999999;
    public int rangeTo = 9999999;

    public UserInput(JComponent con, boolean require,String error,int type, boolean isNumber)
    {
        control = con;
        isRequire = require;
        ErrorText = error;
        ControlType = type;
        isNumberOnly = isNumber;
    }

    public static String Validate(Vector<UserInput> inputs)
    {
        for (UserInput input : inputs)
        {
            if (input.ControlType == CT_TEXTBOX)
            {
                JTextField cur = (JTextField)input.control;

                if (cur.getText().length() > input.maxLength)
                    return input.ErrorText;

                if (cur.getText().length() < input.minLength)
                    return input.ErrorText;
                
                if (input.isRequire && cur.getText().equals(""))
                    return input.ErrorText;
                
                if (input.isNumberOnly && !Utilities.SimpleRegexSingle("([^0-9]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;
                
                if (input.isStringOnly && !Utilities.SimpleRegexSingle("([^a-zA-Z0-9\\s]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;
                
                if (!input.allowChars.equals("") && !Utilities.SimpleRegexSingle("([^"+input.allowChars+"]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;

                if (Utilities.SimpleRegexSingle("([^0-9]*)", cur.getText(), 1).equals(""))
                    if (Integer.parseInt(cur.getText()) < input.rangeFrom || Integer.parseInt(cur.getText()) > input.rangeTo)
                        return input.ErrorText;
            }
            else if (input.ControlType == CT_PASSWORD)
            {
                JPasswordField cur = (JPasswordField)input.control;

                if (cur.getText().length() > input.maxLength)
                    return input.ErrorText;

                if (input.isRequire && cur.getText().equals(""))
                    return input.ErrorText;

                if (input.isNumberOnly && !Utilities.SimpleRegexSingle("([^0-9]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;

                if (input.isStringOnly && !Utilities.SimpleRegexSingle("([^a-zA-Z0-9\\s]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;

                if (!input.allowChars.equals("") && !Utilities.SimpleRegexSingle("([^"+input.allowChars+"]*)", cur.getText(), 1).equals(""))
                    return input.ErrorText;
            }
            else if (input.ControlType == CT_COMBOBOX)
            {
                JComboBox cur = (JComboBox)input.control;

                if (input.isRequire && cur.getSelectedIndex() <= 0)
                    return input.ErrorText;
            }
        }
        return "";
    }
}
