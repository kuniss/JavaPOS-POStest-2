package postest2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import jpos.BaseJposControl;
import jpos.JposConst;
import jpos.JposException;
import jpos.events.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;

public abstract class BaseController implements Initializable, DataListener, StatusUpdateListener {

    /* ************************************************************************
     * ************************ Action Handler ********************************
     * ************************************************************************
     */

    // Common
    @FXML
    public ComboBox<String> logicalName;
    @FXML
    public Button buttonOpen;
    @FXML
    @RequiredState(JposState.OPENED)
    public Button buttonClaim;
    @FXML
    @RequiredState(JposState.CLAIMED)
    public Button buttonRelease;
    @FXML
    @RequiredState(JposState.ENABLED)
    public Button buttonStatistics;
    @FXML
    @RequiredState(JposState.OPENED)
    public Button buttonClose;
    @FXML
    @RequiredState(JposState.ENABLED)
    public Button buttonFirmware;
    @FXML
    @RequiredState(JposState.CLOSED)
    public Button buttonOCE;
    @FXML
    @RequiredState(JposState.ENABLED)
    public Button buttonInfo;
    @FXML
    public Text statusLabel;
    @FXML
    public Text powerLabel;
    @FXML
    @RequiredState(JposState.ENABLED)
    public CheckBox freezeEvents;
    @FXML
    @RequiredState(JposState.OPENED)
    public CheckBox dataEventEnabled;
    @FXML
    @RequiredState(JposState.OPENEDNOTENABLED)
    public CheckBox powerNotify;

    // DirectIO
    @FXML
    public TextField directIO_command;
    @FXML
    public TextField directIO_data;
    @FXML
    public TextField directIO_object;
    @FXML
    public RadioButton directIO_datatypeString;
    @FXML
    public RadioButton directIO_datatypeByteArray;

    // Group for the radiobuttons
    @FXML
    public final ToggleGroup directIO_datatypeGroup = new ToggleGroup();

    BaseJposControl service;

    static String statistics = "";

    @FXML
    public void handleOpen(ActionEvent e) {
        directIO_datatypeByteArray.setToggleGroup(directIO_datatypeGroup);
        directIO_datatypeString.setToggleGroup(directIO_datatypeGroup);
        directIO_datatypeByteArray.setSelected(true);


        POSTest2.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent arg0) {
                try {
                    if (getDeviceState(service) != JposState.CLOSED) {
                        try {
                            service.close();
                        } catch (JposException e) {
                            JOptionPane.showMessageDialog(null, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } catch (HeadlessException e) {
                    e.printStackTrace();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            if (logicalName.getValue() != null && !logicalName.getValue().isEmpty()) {
                service.open(logicalName.getValue());
                RequiredStateChecker.invokeThis(this, service);
                setStatusLabel();
            } else {
                JOptionPane.showMessageDialog(null, "Choose a device!", "Logical name is empty",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (JposException je) {
            je.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to open \"" + logicalName.getSelectionModel().getSelectedItem()
                    + "\"\nException: " + je.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void handleClose(ActionEvent e) {
        try {
            service.close();
            powerNotify.setSelected(false);
            powerLabel.setText("JPOS_PS_UNKNOWN");
        } catch (JposException je) {
            je.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to close \""
                            + logicalName.getSelectionModel().getSelectedItem() + "\"\nException: " + je.getMessage(),
                    "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void handleClaim(ActionEvent e) {
        try {
            service.claim(0);
            RequiredStateChecker.invokeThis(this, service);
        } catch (JposException je) {
            je.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to claim \""
                            + logicalName.getSelectionModel().getSelectedItem() + "\"\nException: " + je.getMessage(),
                    "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void handleRelease(ActionEvent e) {
    }

    public void handleInfo(ActionEvent e) {

    }

    public void handleStatistics(ActionEvent e) {

    }

    // Method to parse the String XML and print the data for the
    // handleStatistics function
    public static void printStatistics(Node e, String tab) {
        if (e.getNodeType() == Node.TEXT_NODE) {
            statistics += tab + e.getNodeValue() + "\n";
            return;
        }

        if (!(e.getNodeName().equals("Name") || e.getNodeName().equals("Value") || e.getNodeName().equals("UPOSStat")
                || e.getNodeName().equals("Event") || e.getNodeName().equals("Equipment") || e.getNodeName().equals(
                "Parameter")))
            statistics += tab + e.getNodeName();

        if (e.getNodeValue() != null) {
            statistics += tab + " " + e.getNodeValue();
        }

        NodeList childs = e.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            printStatistics(childs.item(i), " ");
        }
    }

    @FXML
    public void handleFirmware(ActionEvent e) {
        try {
            FirmwareUpdateDlg dlg = new FirmwareUpdateDlg(service);
            dlg.setVisible(true);
        } catch (Exception e2) {
            e2.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception: " + e2.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FXML
    public void handleFreezeEvents(ActionEvent e) {
        try {
            service.setFreezeEvents(freezeEvents.selectedProperty().getValue());
        } catch (JposException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage());
        }
    }

    @FXML
    public void handleDataEventEnabled(ActionEvent e) {
        Method setDataEventEnabled = getMethod(service, "setDataEventEnabled");
        try {
            if (setDataEventEnabled != null) {
                setDataEventEnabled.invoke(service, dataEventEnabled.selectedProperty().getValue());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage());
        }
    }

    @FXML
    public void handlePowerNotify(ActionEvent e) {
        try {
            Method setPowerNotify = getMethod(service, "setPowerNotify");
            Method getCapPowerReporting = getMethod(service, "getCapPowerReporting");
            Method getPowerNotify = getMethod(service, "getPowerNotify");
            if (setPowerNotify != null && getCapPowerReporting != null && getPowerNotify != null) {
                int capPowerReporting = (int) getCapPowerReporting.invoke(service);
                int notify = powerNotify.selectedProperty().getValue() ? JposConst.JPOS_PN_ENABLED : JposConst.JPOS_PN_DISABLED;
                if (capPowerReporting != JposConst.JPOS_PR_NONE && notify != (int) getPowerNotify.invoke(service))
                    setPowerNotify.invoke(service, notify);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage());
        }
    }

    Method getMethod(Object object, String name) {
        try {
            Method methods[] = Class.forName(object.getClass().getName()).getMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void handleBrowseDirectIOData(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose DirectIOData");
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            directIO_data.setText(f.getAbsolutePath());
        }
    }

    @FXML
    public void handleBrowseDirectIOObject(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose DirectIOData");
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            directIO_object.setText(f.getAbsolutePath());
        }
    }

    @FXML
    public void handleDirectIO(ActionEvent e) {
        if (directIO_command.getText().isEmpty() || directIO_data.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null, "One of the numeric parameters is not specified!");
        } else {
            try {
                File fileData = new File(directIO_data.getText());
                int[] dataArrInt = null;
                if(fileData.exists()){
                    // Reads content from File
                    dataArrInt = readIntsFromFile(directIO_data.getText());
                } else {
                    String[] dataArrString = directIO_data.getText().split(",");
                    dataArrInt = new int[dataArrString.length];
                    for (int i = 0; i < dataArrString.length; i++) {
                        dataArrInt[i] = Integer.parseInt(dataArrString[i]);
                    }
                }
                Object object = null;

                File fileObject = new File(directIO_object.getText());
                if(fileObject.exists()){
                    if(directIO_datatypeByteArray.isSelected()){
                        object = readBytesFromFile(directIO_object.getText());
                    } else if(directIO_datatypeString.isSelected()){
                        object = convertBytesToString(readBytesFromFile(directIO_object.getText()));
                    }
                } else {
                    if(directIO_datatypeByteArray.isSelected()){
                        String[] objArrString = directIO_object.getText().split(",");
                        byte[] objectArr = new byte[objArrString.length];
                        for (int i = 0; i < objArrString.length; i++) {
                            objectArr[i] = Byte.parseByte(objArrString[i]);
                        }
                        object = objectArr;
                    } else if(directIO_datatypeString.isSelected()){
                        object = directIO_object.getText();
                    }
                }

                // Execute DirectIO
                service.directIO(Integer.parseInt(directIO_command.getText()), dataArrInt, object);

            } catch (JposException e1) {
                JOptionPane.showMessageDialog(null, e1.getMessage());
                e1.printStackTrace();
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(null, e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

    /**
     * Set StatusLabel corresponding to the Device Status
     */
    void setStatusLabel() {
        if (service.getState() == JposConst.JPOS_S_IDLE) {
            statusLabel.setText("JPOS_S_IDLE");
        }

        if (service.getState() == JposConst.JPOS_S_CLOSED) {
            statusLabel.setText("JPOS_S_CLOSED");
        }

        if (service.getState() == JposConst.JPOS_S_BUSY) {
            statusLabel.setText("JPOS_S_BUSY");
        }

        if (service.getState() == JposConst.JPOS_S_ERROR) {
            statusLabel.setText("JPOS_S_ERROR");
        }

    }

    /**
     * Set PowerState label corresponding to the Power status
     */
    public void setPowerLabel() {
        Method getPowerState = getMethod(service, "getPowerState");
        int powerState;
        try {
            powerState = (int) getPowerState.invoke(service);
        } catch (Exception e) {
            powerState = JposConst.JPOS_PS_UNKNOWN;
        }

        if (powerState == JposConst.JPOS_PS_UNKNOWN) {
            powerLabel.setText("JPOS_PS_UNKNOWN");
        }

        if (powerState == JposConst.JPOS_PS_ONLINE) {
            powerLabel.setText("JPOS_PS_ONLINE");
        }

        if (powerState == JposConst.JPOS_PS_OFF) {
            powerLabel.setText("JPOS_PS_OFF");
        }

        if (powerState == JposConst.JPOS_PS_OFFLINE) {
            powerLabel.setText("JPOS_PS_OFFLINE");
        }

        if (powerState == JposConst.JPOS_PS_OFF_OFFLINE) {
            powerLabel.setText("JPOS_PS_OFF_OFFLINE");
        }

    }

    protected void setUpLogicalNameComboBox(String devCategory) {
        if (!LogicalNameGetter.getLogicalNamesByCategory(devCategory).isEmpty()) {
            logicalName.setItems(LogicalNameGetter.getLogicalNamesByCategory(devCategory));
        }
    }

    /**
     * Read the given binary file, and return its contents as a byte array.
     *
     */
    protected static byte[] readBytesFromFile(String aInputFileName) {
        File file = new File(aInputFileName);
        byte[] result = new byte[(int) file.length()];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(file));
                while (totalBytesRead < result.length) {
                    int bytesRemaining = result.length - totalBytesRead;
                    // input.read() returns -1, 0, or more :
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0) {
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException ex) {

            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();

        }
        return result;
    }

    /**
     * Read the given binary file, and return its contents as a byte array.
     *
     */
    protected static int[] readIntsFromFile(String aInputFileName) {
        File file = new File(aInputFileName);
        int[] result = new int[(int) file.length()];
        try {
            InputStream input = null;
            try {
                input = new BufferedInputStream(new FileInputStream(file));
                int readInt;
                int num= 0;
                while ((readInt = input.read()) != -1) {
                    result[num] = readInt;
                    num ++;
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();

        }
        return result;
    }

    /**
     * Converty a byte[] to a String
     *
     */
    protected static String convertBytesToString(byte[] bytesFromFile) {
        String ret = "";

        for (int i = 0; i < bytesFromFile.length; i++) {
            if (i != 0) {
                ret += ",";
            }
            ret += (int) bytesFromFile[i];
        }

        return ret;
    }

    /**
     * Write a byte array to the given file. Writing binary data is
     * significantly simpler than reading it.
     */
    protected static void writeBytesToFile(byte[] aInput, String aOutputFileName) {
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
                output.write(aInput);
            } finally {
                output.close();
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sets the tooltips for the common buttons (Open, Claim, Release, Close, ..)
     */
    protected void setUpTooltips() {
        buttonClaim.setTooltip(new Tooltip("Claims the Device"));
        buttonClose.setTooltip(new Tooltip("Closes the connection to the Device"));
        buttonFirmware.setTooltip(new Tooltip("Update or view the Firmware Version of the Device"));
        buttonInfo.setTooltip(new Tooltip("Shows Information about the Device"));
        buttonOCE.setTooltip(new Tooltip("Open, Claims and Enables the Device"));
        buttonOpen.setTooltip(new Tooltip("Opens the Device"));
        buttonRelease.setTooltip(new Tooltip("Releases the Device"));
        buttonStatistics.setTooltip(new Tooltip("View, reset or update Device Statistics"));
    }

    /**
     * Gets the current DeviceState
     *
     * @param service
     * @return
     * @throws JposException
     */
    protected static JposState getDeviceState(BaseJposControl service) throws JposException {
        JposState deviceState = null;
        try {
            if (!service.getClaimed()) {
                deviceState = JposState.OPENED;
            }
            if (service.getClaimed()) {
                deviceState = JposState.CLAIMED;
            }

            if (service.getDeviceEnabled()) {
                deviceState = JposState.ENABLED;
            }
        } catch (JposException e) {
            if (e.getErrorCode() == JposConst.JPOS_E_CLOSED) {
                deviceState = JposState.CLOSED;
            }
        }
        return deviceState;

    }

    @Override
    public void dataOccurred(DataEvent dataEvent) {
        dataEventEnabled.setSelected(false);
    }

    @Override
    public void statusUpdateOccurred(StatusUpdateEvent sue) {
        setPowerLabel();
    }

}
