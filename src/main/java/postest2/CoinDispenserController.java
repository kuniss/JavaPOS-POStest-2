package postest2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javafx.scene.text.Text;
import jpos.CoinDispenser;
import jpos.CoinDispenserConst;
import jpos.JposException;

import jpos.events.StatusUpdateEvent;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CoinDispenserController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public Pane functionPane;

	@FXML
	public TextField readCashCount_cashCount;
	@FXML
	public Label readCashCount_discrepancy;
	@FXML
	@RequiredState(JposState.ENABLED)
	public Label dispenserStatusLabel;
	@FXML
	@RequiredState(JposState.ENABLED)
	public Text dispenserStatus;

	@FXML
	public TextField adjustCashCounts;
	@FXML
	public TextField dispenseCash_cashCounts;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpTooltips();
		service = new CoinDispenser();
		((CoinDispenser) service).addStatusUpdateListener(this);
		RequiredStateChecker.invokeThis(this, service);
		setUpLogicalNameComboBox("CoinDispenser");

	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	@Override
	public void handleRelease(ActionEvent e) {
		super.handleRelease(e);
		dispenserStatus.setText("");
	}

	@Override
	public void handleClose(ActionEvent e) {
		super.handleClose(e);
		dispenserStatus.setText("");
	}

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((CoinDispenser) service).setDeviceEnabled(true);
				readCashCount_cashCount.setEditable(false);
			} else {
				((CoinDispenser) service).setDeviceEnabled(false);
				dispenserStatus.setText("");
			}
		} catch (JposException je) {
			System.err.println("CoinDispenserPanel: CheckBoxListener: Jpos Exception" + je);
		}
		RequiredStateChecker.invokeThis(this, service);
		setupGuiObjects();
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleInfo(ActionEvent e) {
		try {
			IMapWrapper cdcm = new CoinDispenserConstantMapper();
			String msg = DeviceProperties.getProperties(service, cdcm);
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
			JOptionPane.showMessageDialog(null, jpe.getMessage());
			jpe.printStackTrace();
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
			((CoinDispenser) service).retrieveStatistics(stats);
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
	public void handleAdjustCashCounts(ActionEvent e) {
		if (!adjustCashCounts.getText().isEmpty()) {
			try {
				((CoinDispenser) service).adjustCashCounts(adjustCashCounts.getText());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleDispenseCash(ActionEvent e) {
		if (!dispenseCash_cashCounts.getText().isEmpty()) {
			try {
				((CoinDispenser) service).dispenseChange(Integer.parseInt(dispenseCash_cashCounts.getText()));
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleReadCashCount(ActionEvent e) {
		String[] cashCounts = new String[1];
		boolean[] discrepancy = new boolean[1];
		try {
			((CoinDispenser) service).readCashCounts(cashCounts, discrepancy);
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
		this.readCashCount_cashCount.setText(cashCounts[0]);
		this.readCashCount_discrepancy.setText("" + discrepancy[0]);
	}

	@Override
	public void statusUpdateOccurred(StatusUpdateEvent ev) {
		super.statusUpdateOccurred(ev);
		setupGuiObjects();
	}

	@Override
	public void setupGuiObjects() {
		super.setupGuiObjects();
		dispenserStatus.setText(DeviceProperties.getPropertyValue(service, new CoinDispenserConstantMapper(), "getDispenserStatus"));
	}

	/*
	 * ComboBoxes
	 */

}