/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ObjectEditor.java
 *
 * Created on 27-set-2010, 14.11.08
 */
package it.freedomotic.frontend;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.AlreadyExistentException;
import it.freedomotic.exceptions.NotValidElementException;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.geometry.FreedomShape;
import it.freedomotic.model.object.Behavior;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.Representation;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.util.PropertiesPanel;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.BooleanBehaviorLogic;
import it.freedomotic.objects.EnvObjectFactory;
import it.freedomotic.objects.ListBehaviorLogic;
import it.freedomotic.objects.RangedIntBehaviorLogic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.util.*;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author enrico
 */
public class ObjectEditor extends javax.swing.JFrame {

    private EnvObjectLogic object;
    private PropertiesPanel commandsControlPanel;
    private PropertiesPanel triggersControlPanel;
    private PropertiesPanel controlPanel;
    private String oldName;

    /**
     * Creates new form ObjectEditor
     */
    public ObjectEditor(final EnvObjectLogic obj) {
        this.object = obj;
        oldName = object.getPojo().getName();
        EnvObject pojo = obj.getPojo();
        initComponents();
        setSize(600, 400);
        txtName.setText(pojo.getName());
        txtDescription.setText(pojo.getDescription());
        txtProtocol.setText(pojo.getProtocol());
        txtAddress.setText(pojo.getPhisicalAddress());
        Integer x = (int) pojo.getCurrentRepresentation().getOffset().getX();
        Integer y = (int) pojo.getCurrentRepresentation().getOffset().getY();
        Integer rotation = (int) pojo.getCurrentRepresentation().getRotation();
        SpinnerModel modelX =
                new SpinnerNumberModel(0, //initial value
                -100, //min
                (int) Freedomotic.environment.getPojo().getWidth() + 100, //max= env dimension + 1 meter
                1); //step
        SpinnerModel modelY =
                new SpinnerNumberModel(0, //initial value
                -100, //min
                (int) Freedomotic.environment.getPojo().getWidth() + 100, //max
                1); //step
        SpinnerModel modelRotation =
                new SpinnerNumberModel(0, //initial value
                0, //min
                360, //max
                10); //step
        spnX.setModel(modelX);
        spnY.setModel(modelY);
        spnX.setValue((Integer) x);
        spnY.setValue((Integer) y);
        SpinnerModel scaleWidthModel =
                new SpinnerNumberModel(
                new Double(pojo.getCurrentRepresentation().getScaleX()), //initial value
                new Double(0.1), //min
                new Double(10.0), //max
                new Double(0.1)); //step
        spnScaleWidth.setModel(scaleWidthModel);
        SpinnerModel scaleHeightModel =
                new SpinnerNumberModel(
                new Double(pojo.getCurrentRepresentation().getScaleY()), //initial value
                new Double(0.1), //min
                new Double(10.0), //max
                new Double(0.1)); //step
        spnScaleHeight.setModel(scaleHeightModel);
        spnRotation.setValue(rotation);

        populateCommandsPanel();
        populateTriggersPanel();

        //population combo box representation
        DefaultComboBoxModel representationsModel = new DefaultComboBoxModel();
        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
            for (Representation rep : object.getPojo().getRepresentations()) {
                representationsModel.addElement(rep);
            }
        }

        if (pojo.getActAs() != null && !pojo.getActAs().isEmpty() && !pojo.getActAs().equalsIgnoreCase("unimplemented")) {
            setTitle(pojo.getName() + " (" + pojo.getActAs() + " " + pojo.getSimpleType() + ")");
        } else {
            setTitle(pojo.getName() + " (" + pojo.getSimpleType() + ")");
        }

        controlPanel = new PropertiesPanel();
        controlPanel.setKeysEditable(false);
        tabControls.add(controlPanel);

        //create an array of controllers for the object behaviors
        for (BehaviorLogic b : object.getBehaviors()) {
            if (b instanceof BooleanBehaviorLogic) {
                final BooleanBehaviorLogic bb = (BooleanBehaviorLogic) b;
                final JButton button;
                if (bb.getValue()) {
                    button = new JButton("Set " + bb.getName() + " false");
                } else {
                    button = new JButton("Set " + bb.getName() + " true");
                }
                controlPanel.addRow("Change property " + b.getName() + ":", button);
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Config params = new Config();
                        params.setProperty("value", new Boolean(!bb.getValue()).toString());
                        bb.filterParams(params, true);
                        if (bb.getValue()) {
                            button.setText("Set " + bb.getName() + " false");
                        } else {
                            button.setText("Set " + bb.getName() + " true");
                        }
                    }
                });
            }
            if (b instanceof RangedIntBehaviorLogic) {
                final RangedIntBehaviorLogic rb = (RangedIntBehaviorLogic) b;
                final JSlider slider = new JSlider();
                slider.setValue(rb.getValue());
                slider.setMaximum(rb.getMax());
                slider.setMinimum(rb.getMin());
                slider.setPaintTicks(true);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(10);
                slider.setSnapToTicks(true);
                controlPanel.addRow("Change property " + b.getName() + ":", slider);
                slider.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (!slider.getValueIsAdjusting()) {
                            Config params = new Config();
                            params.setProperty("value", new Integer(slider.getValue()).toString());
                            rb.filterParams(params, true);
                        }
                    }
                });
            }
            if (b instanceof ListBehaviorLogic) {
                final ListBehaviorLogic lb = (ListBehaviorLogic) b;
                final JComboBox comboBox = new JComboBox();
                for (String listValue : lb.getValuesList()) {
                    comboBox.addItem(listValue);
                }
                comboBox.setEditable(false);
                comboBox.setSelectedItem(lb.getSelected());
                controlPanel.addRow("Change property " + b.getName() + ":", comboBox);

                comboBox.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        Config params = new Config();
                        params.setProperty("value", (String) comboBox.getSelectedItem());
                        lb.filterParams(params, true);
                    }
                });
            }



        }

//        // Create bespoke component for rendering the tab.
//        JLabel lbl = new JLabel("Hello, World");
//        System.out.println(new File(Info.getResourcesPath() + "/person.png").getAbsolutePath());
//        Icon icon = new ImageIcon(getClass().getResource(new File(Info.getResourcesPath() + "/tex.jpg").getAbsolutePath()));
//        lbl.setIcon(icon);
//
//// Add some spacing between text and icon, and position text to the RHS.
//        lbl.setIconTextGap(5);
//        lbl.setHorizontalTextPosition(SwingConstants.RIGHT);
//
//// Assign bespoke tab component for first tab.
//        jTabbedPane3.setTabComponentAt(0, lbl);


        this.pack();
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane3 = new javax.swing.JTabbedPane();
        tabRepresentation = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        spnX = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spnY = new javax.swing.JSpinner();
        spnRotation = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        chkAllRepresentations = new javax.swing.JCheckBox();
        btnChangeImage = new javax.swing.JButton();
        btnCreateReaction = new javax.swing.JButton();
        spnScaleWidth = new javax.swing.JSpinner();
        spnScaleHeight = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tabControls = new javax.swing.JPanel();
        tabCommandsConfig = new javax.swing.JPanel();
        tabTriggersConfig = new javax.swing.JPanel();
        tabProperties = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtDescription = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtProtocol = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnCreateObjectCopy = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();

        setTitle("Object Editor");
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(500, 300));

        jTabbedPane3.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane3.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane3.setPreferredSize(new java.awt.Dimension(500, 457));

        jLabel11.setText("Position X:");

        spnX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnXStateChanged(evt);
            }
        });

        jLabel12.setText("Position Y:");

        spnY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnYStateChanged(evt);
            }
        });

        spnRotation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnRotationStateChanged(evt);
            }
        });

        jLabel13.setText("Rotation:");

        chkAllRepresentations.setText("Apply changes to all representations");
        chkAllRepresentations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAllRepresentationsActionPerformed(evt);
            }
        });

        btnChangeImage.setText("Change Image");
        btnChangeImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeImageActionPerformed(evt);
            }
        });

        btnCreateReaction.setText("Create a Reaction");
        btnCreateReaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateReactionActionPerformed(evt);
            }
        });

        spnScaleWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnScaleWidthStateChanged(evt);
            }
        });

        spnScaleHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnScaleHeightStateChanged(evt);
            }
        });

        jLabel4.setText("Width:");

        jLabel5.setText("Height:");

        javax.swing.GroupLayout tabRepresentationLayout = new javax.swing.GroupLayout(tabRepresentation);
        tabRepresentation.setLayout(tabRepresentationLayout);
        tabRepresentationLayout.setHorizontalGroup(
            tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabRepresentationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabRepresentationLayout.createSequentialGroup()
                        .addComponent(chkAllRepresentations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18))
                    .addGroup(tabRepresentationLayout.createSequentialGroup()
                        .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spnRotation, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnScaleHeight, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnScaleWidth, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnY)
                            .addComponent(spnX, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(18, 18, 18)
                        .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnChangeImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCreateReaction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(2343, 2343, 2343))))
        );
        tabRepresentationLayout.setVerticalGroup(
            tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabRepresentationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkAllRepresentations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(spnX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangeImage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCreateReaction)
                    .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(spnY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabRepresentationLayout.createSequentialGroup()
                        .addComponent(spnRotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spnScaleWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)))
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabRepresentationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnScaleHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(484, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Representation", tabRepresentation);

        tabControls.setLayout(new java.awt.BorderLayout());
        jTabbedPane3.addTab("Control Panel", tabControls);

        tabCommandsConfig.setLayout(new java.awt.BorderLayout());
        jTabbedPane3.addTab("Commands Configuration", tabCommandsConfig);

        tabTriggersConfig.setLayout(new java.awt.BorderLayout());
        jTabbedPane3.addTab("Triggers Configuration", tabTriggersConfig);

        jLabel14.setText("Name:");

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        jLabel15.setText("Description:");

        jLabel1.setText("Protocol:");

        jLabel2.setText("Address:");

        jLabel3.setForeground(new java.awt.Color(121, 121, 121));
        jLabel3.setText("for more info www.freedomotic.com/plugins");

        btnCreateObjectCopy.setText("Create a copy");
        btnCreateObjectCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateObjectCopyActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Object");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabPropertiesLayout = new javax.swing.GroupLayout(tabProperties);
        tabProperties.setLayout(tabPropertiesLayout);
        tabPropertiesLayout.setHorizontalGroup(
            tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabPropertiesLayout.createSequentialGroup()
                        .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtName)
                                .addComponent(txtDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                                .addComponent(txtProtocol)
                                .addComponent(txtAddress))))
                    .addGroup(tabPropertiesLayout.createSequentialGroup()
                        .addComponent(btnCreateObjectCopy, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(2027, Short.MAX_VALUE))
        );
        tabPropertiesLayout.setVerticalGroup(
            tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(tabPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateObjectCopy)
                    .addComponent(btnDelete))
                .addGap(480, 480, 480))
        );

        jTabbedPane3.addTab("Properties", tabProperties);

        getContentPane().add(jTabbedPane3, java.awt.BorderLayout.CENTER);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        getContentPane().add(btnCancel, java.awt.BorderLayout.PAGE_END);

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });
        getContentPane().add(btnOk, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        if ((!txtProtocol.getText().equals(""))
                && (!txtAddress.getText().equals(""))) {
            EnvObject pojo = object.getPojo();
            pojo.setProtocol(txtProtocol.getText());
            pojo.setPhisicalAddress(txtAddress.getText());
            pojo.setDescription(txtDescription.getText());
            if (!(oldName.equals(txtName.getText().trim()))) {
                object.rename(txtName.getText().trim());
            }
            object.setChanged(true);
            saveRepresentationChanges();
            this.dispose();
        }
}//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
}//GEN-LAST:event_btnCancelActionPerformed

    private void btnCreateReactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateReactionActionPerformed
        //we send a command with a show event editor plugin request
        Command showReactionEditor = new Command();
        showReactionEditor.setName("Show automation editor GUI");
        showReactionEditor.setDescription("make a the automation editor plugin show its GUI");
        showReactionEditor.setReceiver("app.actuators.plugins.controller.in");
        showReactionEditor.setProperty("plugin", "Automation Editor"); //the target plugin
        showReactionEditor.setProperty("action", "SHOW");         //the action to perform
        Freedomotic.getScheduler().schedule(showReactionEditor);
        //TODO: make this return a command
        this.dispose();
}//GEN-LAST:event_btnCreateReactionActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        EnvObjectPersistence.remove(object);
        this.dispose();
}//GEN-LAST:event_btnDeleteActionPerformed

    private void btnCreateObjectCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateObjectCopyActionPerformed
        EnvObject pojoCopy = null;

        pojoCopy = SerialClone.clone(object.getPojo());

        pojoCopy.setName(object.getPojo().getName() + UidGenerator.getNextStringUid());
        pojoCopy.setPhisicalAddress("unknown");
        pojoCopy.setProtocol("unknown");
        for (Representation view : pojoCopy.getRepresentations()) {
            view.setOffset(0, 0);
        }
        EnvObjectLogic envObjectLogic = EnvObjectFactory.create(pojoCopy);
        envObjectLogic.getPojo().setUUID("");
        try {
            EnvObjectPersistence.add(envObjectLogic);
        } catch (NotValidElementException ex) {
            Logger.getLogger(ObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AlreadyExistentException ex) {
            Logger.getLogger(ObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        object.setChanged(true);
        this.dispose();
}//GEN-LAST:event_btnCreateObjectCopyActionPerformed

    private void btnChangeImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeImageActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Info.getResourcesPath()));
        int returnVal = fc.showDialog(this, "Use as object icon");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            object.getPojo().getCurrentRepresentation().setIcon(file.getName());
        }
}//GEN-LAST:event_btnChangeImageActionPerformed

    private void chkAllRepresentationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAllRepresentationsActionPerformed
}//GEN-LAST:event_chkAllRepresentationsActionPerformed

    private void spnRotationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnRotationStateChanged
        saveRepresentationChanges();
}//GEN-LAST:event_spnRotationStateChanged

    private void spnYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnYStateChanged
        saveRepresentationChanges();
}//GEN-LAST:event_spnYStateChanged

    private void spnXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnXStateChanged
        saveRepresentationChanges();
}//GEN-LAST:event_spnXStateChanged

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO addAndRegister your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void spnScaleWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnScaleWidthStateChanged
        Representation rep = object.getPojo().getCurrentRepresentation();
//        Shape shape = AWTConverter.convertToAWT(rep.getShape(), 1.5, 1.5);
//                new Double(spnScaleWidth.getValue().toString()).doubleValue(), 
//                new Double(spnScaleHeight.getValue().toString()).doubleValue());
        rep.setScaleX(new Double(spnScaleWidth.getValue().toString()).doubleValue());
        //rep.setScaleX(new Double(spnScaleWidth.getValue().toString()).doubleValue());
        object.setChanged(true);
    }//GEN-LAST:event_spnScaleWidthStateChanged

    private void spnScaleHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnScaleHeightStateChanged
        saveRepresentationChanges();
    }//GEN-LAST:event_spnScaleHeightStateChanged

    private void changeCurrentRepresentation() {
        try {
            Representation b = object.getPojo().getCurrentRepresentation();
            int x = (Integer) spnX.getValue();
            int y = (Integer) spnY.getValue();
            int rotation = (Integer) spnRotation.getValue();
            b.setOffset(x, y);
            b.setRotation(rotation);
            object.setChanged(true);
        } catch (NumberFormatException numberFormatException) {
        }
    }

    private void saveRepresentationChanges() {
        if (chkAllRepresentations.isSelected()) {
            changeAllRepresentations();
        } else {
            changeCurrentRepresentation();
        }
    }

    private void changeAllRepresentations() {
        try {
            for (Representation b : this.object.getPojo().getRepresentations()) {
                int x = (Integer) spnX.getValue();
                int y = (Integer) spnY.getValue();
                int rotation = (Integer) spnRotation.getValue();
                b.setOffset(x, y);
                b.setRotation(rotation);
            }
            object.setChanged(true);
        } catch (NumberFormatException numberFormatException) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChangeImage;
    private javax.swing.JButton btnCreateObjectCopy;
    private javax.swing.JButton btnCreateReaction;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnOk;
    private javax.swing.JCheckBox chkAllRepresentations;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JSpinner spnRotation;
    private javax.swing.JSpinner spnScaleHeight;
    private javax.swing.JSpinner spnScaleWidth;
    private javax.swing.JSpinner spnX;
    private javax.swing.JSpinner spnY;
    private javax.swing.JPanel tabCommandsConfig;
    private javax.swing.JPanel tabControls;
    private javax.swing.JPanel tabProperties;
    private javax.swing.JPanel tabRepresentation;
    private javax.swing.JPanel tabTriggersConfig;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtProtocol;
    // End of variables declaration//GEN-END:variables

    private void populateCommandsPanel() {
        //addAndRegister a properties panel
        commandsControlPanel = new PropertiesPanel();
        commandsControlPanel.setKeysEditable(false);
        tabCommandsConfig.add(commandsControlPanel);

        //creates an array of mapping behavior to hardware command
        for (final String action : object.getPojo().getActions().stringPropertyNames()) {
            //addAndRegister a combo box with the list of alla available commands
            DefaultComboBoxModel allHardwareCommands = new DefaultComboBoxModel();
            for (Command command : CommandPersistence.getHardwareCommands()) {
                allHardwareCommands.addElement(command);
            }
            final JComboBox cmbCommand = new JComboBox();
            cmbCommand.setModel(allHardwareCommands);
            EnvObjectLogic objLogic = (EnvObjectLogic) object;
            Command relatedCommand = objLogic.getHardwareCommand(action);
            if (relatedCommand != null) {
                //related harware command is already defined
                cmbCommand.setSelectedItem(relatedCommand);
            }else{
                cmbCommand.setSelectedIndex(-1); //no mapping
            }
            cmbCommand.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Command command = (Command) cmbCommand.getSelectedItem();
                    object.setAction(action, command);
                }
            });
            commandsControlPanel.addRow(action, cmbCommand);
        }
    }

    private void populateTriggersPanel() {
        //addAndRegister a properties panel
        triggersControlPanel = new PropertiesPanel();
        triggersControlPanel.setKeysEditable(false);
        tabTriggersConfig.add(triggersControlPanel);

        //creates an array of mapping behavior to hardware trigger
        for (final Behavior behavior : object.getPojo().getBehaviors()) {
            //addAndRegister a combo box with the list of all available hardware triggers
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (Trigger trigger : TriggerPersistence.getTriggers()) {
                if (trigger.isHardwareLevel()) {
                    model.addElement(trigger);
                }
            }
            final JComboBox valueComponent = new JComboBox();
            valueComponent.setModel(model);
            //check if the object has already defined a mapping trigger -> behavior
            //if true select it
            String relatedTriggerName = "";
            for (Entry e : object.getPojo().getTriggers().entrySet()) {
                if (e.getValue().equals(behavior.getName())) {
                    //it has already a mapping
                    relatedTriggerName = (String) e.getKey();
                }
            }
            Trigger relatedTrigger = TriggerPersistence.getTrigger(relatedTriggerName);
            //if related harware trigger is already defined
            if (relatedTrigger != null) {
                valueComponent.setSelectedItem(relatedTrigger);
            } else {
                //no selection
                valueComponent.setSelectedIndex(-1);
            }
            //addAndRegister a listener on trigger combobox item selection
            valueComponent.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    object.addTriggerMapping((Trigger) valueComponent.getSelectedItem(), behavior.getName());
                }
            });
            triggersControlPanel.addRow(behavior.getName(), valueComponent);
        }
    }
}
