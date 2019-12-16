package postest2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jpos.CashChanger;
import jpos.JposException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CashChangerController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public Pane functionPane;

	@FXML
	@RequiredState(JposState.OPENED)
	public CheckBox asyncMode;

	// Controls
	@FXML
	ComboBox<Integer> currentService;

	@FXML
	public ComboBox<String> currencyCode;

	@FXML
	public ComboBox<Boolean> realTimeDataEnabled;

	@FXML
	public ComboBox<String> endDeposit_success;

	@FXML
	public ComboBox<String> pauseDeposit_control;

	@FXML
	public Label readCashCount_cashCount;

	@FXML
	public Label readCashCount_discrepancy;

	@FXML
	public TextField adjustCashCounts;

	@FXML
	public ComboBox<Integer> currentExit;

	@FXML
	public TextField dispenseCash_cashCounts;


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpTooltips();
		service = new CashChanger();
		((CashChanger) service).addStatusUpdateListener(this);
		setUpLogicalNameComboBox("CashChanger");
		RequiredStateChecker.invokeThis(this, service);
	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */
	@Override
	@FXML
	public void handleOCE(ActionEvent e) {
		super.handleOCE(e);
		try {
			if(getDeviceState(service) == JposState.CLAIMED){
				deviceEnabled.setSelected(true);
				handleDeviceEnable(e);
			}
		} catch (JposException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleInfo(ActionEvent e) {
		try {
			IMapWrapper cccm = new CashChangerConstantMapper();
			String msg = DeviceProperties.getProperties(service, cccm);
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
			((CashChanger) service).retrieveStatistics(stats);
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(new java.io.StringReader(stats[1])));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(stats[1].getBytes()));

			printStatistics(doc.getDocumentElement(), "");

			JOptionPane.showMessageDialog(null, statistics, "Statistics", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			JOptionPane.showMessageDialog(null, ioe.getMessage());
		} catch (SAXException saxe) {
			saxe.printStackTrace();
			JOptionPane.showMessageDialog(null, saxe.getMessage());
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException jpe) {
			jpe.printStackTrace();
			JOptionPane.showMessageDialog(null, "Statistics are not supported!", "Statistics",
					JOptionPane.ERROR_MESSAGE);
		}

		statistics = "";
	}

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((CashChanger) service).setDeviceEnabled(true);
				setUpComboBoxes();

			} else {
				((CashChanger) service).setDeviceEnabled(false);
			}
		} catch (JposException je) {
			je.printStackTrace();
			JOptionPane.showMessageDialog(null, je.getMessage());
		}

		RequiredStateChecker.invokeThis(this, service);
	}

	@FXML
	public void handleAsyncMode(ActionEvent e) {
		try {
			((CashChanger) service).setAsyncMode(asyncMode.isSelected());
		} catch (JposException je) {
			JOptionPane.showMessageDialog(null, je.getMessage());
			je.printStackTrace();
		}
	}

	@FXML
	public void handleSetCurrencyCode(ActionEvent e) {
		try {
			((CashChanger) service).setCurrencyCode(currencyCode.getSelectionModel().getSelectedItem());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetRealTimeDataEnabled(ActionEvent e) {
		try {
			((CashChanger) service).setRealTimeDataEnabled(realTimeDataEnabled.getSelectionModel().getSelectedItem());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleAdjustCashCounts(ActionEvent e) {
		if (!adjustCashCounts.getText().isEmpty()) {
			try {
				((CashChanger) service).adjustCashCounts(adjustCashCounts.getText());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleBeginDeposit(ActionEvent e) {
		try {
			((CashChanger) service).beginDeposit();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleEndDeposit(ActionEvent e) {
		try {
			((CashChanger) service).endDeposit(BillAcceptorConstantMapper
					.getConstantNumberFromString(endDeposit_success.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleFixDeposit(ActionEvent e) {
		try {
			((CashChanger) service).fixDeposit();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handlePauseDeposit(ActionEvent e) {
		try {
			((CashChanger) service).pauseDeposit(BillAcceptorConstantMapper
					.getConstantNumberFromString(pauseDeposit_control.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleReadCashCount(ActionEvent e) {
		String[] cashCounts = new String[1];
		boolean[] discrepancy = new boolean[1];
		try {
			((CashChanger) service).readCashCounts(cashCounts, discrepancy);
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
		this.readCashCount_cashCount.setText(cashCounts[0]);
		this.readCashCount_discrepancy.setText("" + discrepancy[0]);
	}

	@FXML
	public void handleSetCurrentExit(ActionEvent e) {
		try {
			((CashChanger) service).setCurrentExit(currentExit.getSelectionModel().getSelectedItem());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleDispenseCash(ActionEvent e) {
		if (!adjustCashCounts.getText().isEmpty()) {
			try {
				((CashChanger) service).adjustCashCounts(adjustCashCounts.getText());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetCurrentService(ActionEvent e) {
		if (!adjustCashCounts.getText().isEmpty()) {
			try {
				((CashChanger) service).setCurrentService(currentService.getSelectionModel().getSelectedItem());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	/*
	 * Set Up all ComboBoxes
	 */

	private void setUpCurrencyCode() {
		String[] currencies = null;
		try {
			currencies = ((CashChanger) service).getDepositCodeList().split(",");
		} catch (JposException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}

		currencyCode.getItems().clear();
		for (int i = 0; i < currencies.length; i++) {
			currencyCode.getItems().add(currencies[i]);
		}
		currencyCode.setValue(currencies[0]);

	}

	private void setUpRealTimeDataEnabled() {

		realTimeDataEnabled.getItems().clear();
		realTimeDataEnabled.getItems().add(true);
		realTimeDataEnabled.getItems().add(false);
		realTimeDataEnabled.setValue(true);

	}

	private void setUpEndDepositSuccess() {
		endDeposit_success.getItems().clear();
		endDeposit_success.getItems().add(BillAcceptorConstantMapper.BACC_DEPOSIT_COMPLETE.getConstant());
		endDeposit_success.setValue(BillAcceptorConstantMapper.BACC_DEPOSIT_COMPLETE.getConstant());
	}

	private void setUpPauseDepositControl() {

		pauseDeposit_control.getItems().clear();
		pauseDeposit_control.getItems().add(BillAcceptorConstantMapper.BACC_DEPOSIT_PAUSE.getConstant());
		pauseDeposit_control.getItems().add(BillAcceptorConstantMapper.BACC_DEPOSIT_RESTART.getConstant());
		pauseDeposit_control.setValue(BillAcceptorConstantMapper.BACC_DEPOSIT_PAUSE.getConstant());
	}

	private void setUpCurrentExit() {
		currentExit.getItems().clear();
		try {
			for (int i = 1; i <= ((CashChanger) service).getDeviceExits(); i++) {
				currentExit.getItems().add(i);
			}
		} catch (JposException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}
		currentExit.setValue(1);
	}

	private void setUpCurrentService() {
		currentService.getItems().clear();
		try {
			for (int i = 0; i <= ((CashChanger) service).getServiceCount(); i++) {
				currentService.getItems().add(i);
			}
		} catch (JposException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}
		currentService.setValue(1);
	}

	private void setUpComboBoxes() {
		setUpCurrencyCode();
		setUpRealTimeDataEnabled();
		setUpEndDepositSuccess();
		setUpPauseDepositControl();
		setUpCurrentExit();
		setUpCurrentService();
	}

}