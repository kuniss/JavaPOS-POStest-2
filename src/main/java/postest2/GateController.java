package postest2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javafx.scene.text.Text;
import jpos.Gate;
import jpos.GateConst;
import jpos.JposException;

import jpos.events.StatusUpdateEvent;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GateController extends SharableController implements Initializable {

	@FXML @RequiredState(JposState.ENABLED)
	public Pane functionPane;

	@FXML 
	public TextField waitForGateClose_timeout;

	@FXML
	@RequiredState(JposState.ENABLED)
	public Text gateStatus;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpTooltips();
		service = new Gate();
		((Gate) service).addStatusUpdateListener(this);
		RequiredStateChecker.invokeThis(this, service);
		setUpLogicalNameComboBox("Gate");
	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((Gate) service).setDeviceEnabled(true);
			} else {
				((Gate) service).setDeviceEnabled(false);
			}
		} catch (JposException je) {
			JOptionPane.showMessageDialog(null, je.getMessage());
			je.printStackTrace();
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
			IMapWrapper gcm = new GateConstantMapper();
			String msg = DeviceProperties.getProperties(service, gcm);
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
			JOptionPane.showMessageDialog(null, "Exception in Info\nException: " + jpe.getMessage(),
					"Exception", JOptionPane.ERROR_MESSAGE);
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
			((Gate) service).retrieveStatistics(stats);
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
	public void handleOpenGate(ActionEvent e) {
		try {
			((Gate)service).openGate();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleWaitForGateClose(ActionEvent e) {
		if(waitForGateClose_timeout.getText().isEmpty()){
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((Gate)service).waitForGateClose(Integer.parseInt(waitForGateClose_timeout.getText()));
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	class GateStatusCodeMapper extends ErrorCodeMapper {
		GateStatusCodeMapper() {
			Mappings = new Object[]{
					GateConst.GATE_GS_CLOSED, "GATE_GS_CLOSED",
					GateConst.GATE_GS_OPEN, "GATE_GS_OPEN",
					GateConst.GATE_GS_BLOCKED, "GATE_GS_BLOCKED",
					GateConst.GATE_GS_MALFUNCTION, "GATE_GS_MALFUNCTION"
			};
		}
	}

	@Override
	public void setupGuiObjects() {
		super.setupGuiObjects();
		try {
			gateStatus.setText(((Gate) service).getCapGateStatus()
					? new GateStatusCodeMapper().getName(((Gate) service).getGetStatus())
					: "");
		} catch (JposException e) {
			gateStatus.setText("");
		}
	}

	@Override
	public void statusUpdateOccurred(StatusUpdateEvent event) {
		super.statusUpdateOccurred(event);
		setupGuiObjects();
	}
}