package postest2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jpos.ItemDispenser;
import jpos.JposException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ItemDispenserController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public Pane functionPane;

	@FXML
	ComboBox<Integer> adjustItemCount_slotNumber;
	@FXML
	ComboBox<Integer> dispenseItem_slotNumber;
	@FXML
	ComboBox<Integer> readItemCount_slotNumber;

	@FXML
	TextField adjustItemCount_itemCount;
	@FXML
	TextField dispenseItem_numItem;
	@FXML
	TextField readItemCount_itemCount;


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpTooltips();
		service = new ItemDispenser();
		((ItemDispenser) service).addStatusUpdateListener(this);
		RequiredStateChecker.invokeThis(this, service);
		setUpLogicalNameComboBox("ItemDispenser");
	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((ItemDispenser) service).setDeviceEnabled(true);
			} else {
				((ItemDispenser) service).setDeviceEnabled(false);
			}
		} catch (JposException je) {
			JOptionPane.showMessageDialog(null, je.getMessage());
			je.printStackTrace();
		}
		setupGuiObjects();
		RequiredStateChecker.invokeThis(this, service);
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleInfo(ActionEvent e) {
		try {
			String msg = DeviceProperties.getProperties(service, null);
			JTextArea jta = new JTextArea(msg);
			@SuppressWarnings("serial")
			JScrollPane jsp = new JScrollPane(jta) {
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(460, 390);
				}
			};
			JOptionPane.showMessageDialog(null, jsp, "Information", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception jpe) {
			JOptionPane.showMessageDialog(null, "Exception in Info\nException: " + jpe.getMessage(), "Exception",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("Jpos exception " + jpe);
		}
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleStatistics(ActionEvent e) {
		String[] stats = new String[] { "", "U_", "M_" };
		try {
			((ItemDispenser) service).retrieveStatistics(stats);
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(new java.io.StringReader(stats[1])));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(stats[1].getBytes()));

			printStatistics(doc.getDocumentElement(), "");

			JOptionPane.showMessageDialog(null, statistics, "Statistics", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, ioe.getMessage());
			ioe.printStackTrace();
		} catch (SAXException saxe) {
			JOptionPane.showMessageDialog(null, saxe.getMessage());
			saxe.printStackTrace();
		} catch (ParserConfigurationException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		} catch (JposException jpe) {
			jpe.printStackTrace();
			JOptionPane.showMessageDialog(null, "Statistics are not supported!", "Statistics",
					JOptionPane.ERROR_MESSAGE);
		}

		statistics = "";
	}
	
	@FXML
	public void handleAdjustItemCount(ActionEvent e) {
		if(adjustItemCount_itemCount.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((ItemDispenser)service).adjustItemCount(Integer.parseInt(adjustItemCount_itemCount.getText()), adjustItemCount_slotNumber.getSelectionModel().getSelectedItem());
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}
	
	@FXML
	public void handleDispenseItem(ActionEvent e) {
		if(dispenseItem_numItem.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			int[] numItem = new int [1];
			numItem[0] = Integer.parseInt(dispenseItem_numItem.getText());
			try {
				((ItemDispenser)service).dispenseItem(numItem, dispenseItem_slotNumber.getSelectionModel().getSelectedItem());
				dispenseItem_numItem.setText("" + numItem[0]);
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}
	
	@FXML
	public void handleReadItemCount(ActionEvent e) {
		
		int[] numItem = new int [1];
		try {
			((ItemDispenser)service).readItemCount(numItem, readItemCount_slotNumber.getSelectionModel().getSelectedItem());
			readItemCount_itemCount.setText("" + numItem[0]);
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
		
	}

	private void setUpAdjustItemCountSlotNumber() {
		int current = adjustItemCount_slotNumber.getItems().size() > 0
				? adjustItemCount_slotNumber.getValue() : 0;
		adjustItemCount_slotNumber.getItems().clear();
		try {
			for (int i = 1; i <= ((ItemDispenser) service).getMaxSlots(); i++) {
				adjustItemCount_slotNumber.getItems().add(i);
			}
		} catch (JposException e) {
			if (getDeviceState(service) != JposState.CLOSED) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}
		}
		if (current > 0 && current <= adjustItemCount_slotNumber.getItems().size())
			adjustItemCount_slotNumber.getSelectionModel().select(current);
		else
			adjustItemCount_slotNumber.getSelectionModel().select(1);
	}

	private void setUpDispenseItemSlotNumber() {
		int current = dispenseItem_slotNumber.getItems().size() > 0
				? dispenseItem_slotNumber.getValue() : 0;
		dispenseItem_slotNumber.getItems().clear();
		try {
			for (int i = 1; i <= ((ItemDispenser) service).getMaxSlots(); i++) {
				dispenseItem_slotNumber.getItems().add(i);
			}
		} catch (JposException e) {
			if (getDeviceState(service) != JposState.CLOSED) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}
		}
		if (current > 0 && current <= dispenseItem_slotNumber.getItems().size())
			dispenseItem_slotNumber.getSelectionModel().select(current);
		else
			dispenseItem_slotNumber.getSelectionModel().select(1);
	}

	private void setUpReadItemCountSlotNumber() {
		int current = readItemCount_slotNumber.getItems().size() > 0
				? readItemCount_slotNumber.getValue() : 0;
		readItemCount_slotNumber.getItems().clear();
		try {
			for (int i = 1; i <= ((ItemDispenser) service).getMaxSlots(); i++) {
				readItemCount_slotNumber.getItems().add(i);
			}
		} catch (JposException e) {
			if (getDeviceState(service) != JposState.CLOSED) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();
			}
		}
		if (current > 0 && current <= readItemCount_slotNumber.getItems().size())
			readItemCount_slotNumber.getSelectionModel().select(current);
		else
			readItemCount_slotNumber.getSelectionModel().select(1);
	}

	@Override
	public void setupGuiObjects() {
		super.setupGuiObjects();
		setUpAdjustItemCountSlotNumber();
		setUpDispenseItemSlotNumber();
		setUpReadItemCountSlotNumber();
	}
}