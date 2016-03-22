
package com.freedomotic.jfrontend.utils;

import com.freedomotic.rules.Payload;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import java.util.Iterator;
import javax.swing.JTextField;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico Nicoletti
 */
public class PropertiesPanel_1Test {

    static PropertiesPanel_1 panel;
    static Trigger trigger;
    static Trigger result;

    /**
     *
     */
    public PropertiesPanel_1Test() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass()
            throws Exception {
        trigger = new Trigger();
        trigger.getPayload().addStatement("property1", "value1");
        trigger.getPayload().addStatement("property2", "value2");
        trigger.getPayload().addStatement("property3", "value3");
        panel = new PropertiesPanel_1(trigger.getPayload().size(),
                4);

        Iterator it = trigger.getPayload().iterator();
        int row = 0;

        while (it.hasNext()) {
            Statement statement = (Statement) it.next();
            panel.addElement(new JTextField(statement.getLogical()),
                    row,
                    0);
            panel.addElement(new JTextField(statement.getAttribute()),
                    row,
                    1);
            panel.addElement(new JTextField(statement.getOperand()),
                    row,
                    2);
            panel.addElement(new JTextField(statement.getValue()),
                    row,
                    3);
            row++;
        }

        panel.layoutPanel();
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass()
            throws Exception {
    }

    /**
     *
     */
    @Test
    public void testAddElement() {
    }

    /**
     *
     */
    @Test
    public void testLayoutPanel() {
    }

    /**
     *
     */
    @Test
    public void testAddRow() {
    }

    /**
     *
     */
    @Test
    public void testGetRows() {
    }

    /**
     *
     */
    @Test
    public void testGetColumns() {
    }

    /**
     *
     */
    @Test
    public void testGetComponent() {
        result = new Trigger();

        Payload p = new Payload();

        for (int row = 0; row < panel.getRows(); row++) {
            for (int col = 0; col < panel.getColumns(); col++) {
                String logical = panel.getComponent(row, 0);
                String attribute = panel.getComponent(row, 1);
                String operand = panel.getComponent(row, 2);
                String value = panel.getComponent(row, 3);
                p.addStatement(logical, attribute, operand, value);
            }
        }

        result.setName(trigger.getName());
        result.setChannel(trigger.getChannel());
        result.setDescription(trigger.getDescription());
        result.setPersistence(true);
        result.setPayload(p);
        Assert.assertEquals(result, trigger);
    }
}
