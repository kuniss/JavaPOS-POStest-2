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
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jpos.CheckScanner;
import jpos.CheckScannerConst;
import jpos.JposException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CheckScannerController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public TabPane functionPane;

	@FXML
	public ComboBox<String> color;
	@FXML
	public ComboBox<Boolean> concurrentMICR;
	@FXML
	public ComboBox<String> imageFormat;
	@FXML
	public ComboBox<String> mapMode;
	@FXML
	public ComboBox<Integer> quality;
	@FXML
	public ComboBox<String> clearImage_by;
	@FXML
	public ComboBox<String> retrieveImage_cropAreaID;
	@FXML
	public ComboBox<String> defineCropArea_cropAreaID;
	@FXML
	public ComboBox<String> retrieveMemory_by;
	@FXML
	public ComboBox<String> storeImage_cropAreaID;
	@FXML
	public ComboBox<String> defineCropArea_cx;
	@FXML
	public ComboBox<String> defineCropArea_cy;

	@FXML
	public TextField documentHeight;
	@FXML
	public TextField documentWidth;
	@FXML
	public TextField fileID;
	@FXML
	public TextField fileIndex;
	@FXML
	public TextField defineCropArea_x;
	@FXML
	public TextField defineCropArea_y;

	@FXML
	public TextArea imageTagData;

	@FXML
	public Slider contrast;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpTooltips();
		service = new CheckScanner();
		((CheckScanner) service).addStatusUpdateListener(this);
		RequiredStateChecker.invokeThis(this, service);
		setUpLogicalNameComboBox("CheckScanner");
	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((CheckScanner) service).setDeviceEnabled(true);
			} else {
				((CheckScanner) service).setDeviceEnabled(false);
			}
		} catch (JposException je) {
			System.err.println("CheckScannerPanel: CheckBoxListener: Jpos Exception" + je);
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
			IMapWrapper cscm = new CheckScannerConstantMapper();
			String msg = DeviceProperties.getProperties(service, cscm);
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
			((CheckScanner) service).retrieveStatistics(stats);
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
	public void handleSetColor(ActionEvent e) {
		try {
			((CheckScanner) service).setColor(CheckScannerConstantMapper.getConstantNumberFromString(color
					.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetConcurrentMICR(ActionEvent e) {
		try {
			((CheckScanner) service).setConcurrentMICR(concurrentMICR.getSelectionModel().getSelectedItem());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetContrast(ActionEvent e) {
		try {
			((CheckScanner) service).setContrast((int) contrast.getValue());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetDocumentHeight(ActionEvent e) {
		if (documentHeight.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).setDocumentHeight(Integer.parseInt(documentHeight.getText()));
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetDocumentWidth(ActionEvent e) {
		if (documentWidth.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).setDocumentWidth(Integer.parseInt(documentWidth.getText()));
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetFileID(ActionEvent e) {
		if (fileID.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).setFileID(fileID.getText());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetFileIndex(ActionEvent e) {
		if (fileIndex.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).setFileIndex(Integer.parseInt(fileIndex.getText()));
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetImageFormat(ActionEvent e) {
		try {
			((CheckScanner) service).setImageFormat(CheckScannerConstantMapper
					.getConstantNumberFromString(imageFormat.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetImageTagData(ActionEvent e) {
		if (imageTagData.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).setImageTagData(imageTagData.getText());
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleSetMapMode(ActionEvent e) {
		try {
			((CheckScanner) service).setMapMode(CheckScannerConstantMapper
					.getConstantNumberFromString(mapMode.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleSetQuality(ActionEvent e) {
		try {
			((CheckScanner) service).setQuality(quality.getSelectionModel().getSelectedItem());
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleBeginInsertion(ActionEvent e) {
		try {
			((CheckScanner) service).beginInsertion(0);
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleBeginRemoval(ActionEvent e) {
		try {
			((CheckScanner) service).beginRemoval(0);
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleEndInsertion(ActionEvent e) {
		try {
			((CheckScanner) service).endInsertion();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleEndRemoval(ActionEvent e) {
		try {
			((CheckScanner) service).endRemoval();
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleClearImage(ActionEvent e) {
		try {
			((CheckScanner) service).clearImage(CheckScannerConstantMapper
					.getConstantNumberFromString(clearImage_by.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleRetrieveImage(ActionEvent e) {
		try {
			((CheckScanner) service).retrieveImage(CheckScannerConstantMapper
					.getConstantNumberFromString(retrieveImage_cropAreaID.getSelectionModel()
							.getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleDefineCropArea(ActionEvent e) {
		if (defineCropArea_x.getText().isEmpty() || defineCropArea_y.getText().isEmpty()
				|| defineCropArea_cx.getSelectionModel().getSelectedItem().isEmpty()
				|| defineCropArea_cx.getSelectionModel().getSelectedItem().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Field should have a value!");
		} else {
			try {
				((CheckScanner) service).defineCropArea(CheckScannerConstantMapper
						.getConstantNumberFromString(defineCropArea_cropAreaID.getSelectionModel()
								.getSelectedItem()), Integer.parseInt(defineCropArea_x.getText()), Integer
						.parseInt(defineCropArea_y.getText()),
						CheckScannerConstantMapper.getConstantNumberFromString(defineCropArea_cx
								.getSelectionModel().getSelectedItem()), CheckScannerConstantMapper
								.getConstantNumberFromString(defineCropArea_cy.getSelectionModel()
										.getSelectedItem()));
			} catch (JposException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	@FXML
	public void handleRetrieveMemory(ActionEvent e) {
		try {
			((CheckScanner) service).retrieveMemory(CheckScannerConstantMapper
					.getConstantNumberFromString(retrieveMemory_by.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	@FXML
	public void handleStoreImage(ActionEvent e) {
		try {
			((CheckScanner) service)
					.storeImage(CheckScannerConstantMapper.getConstantNumberFromString(storeImage_cropAreaID
							.getSelectionModel().getSelectedItem()));
		} catch (JposException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}

	/*
	 * Set Up Combo Boxes
	 */

	class ColorCodeMapper extends ErrorCodeMapper {
		ColorCodeMapper() {
			Mappings = new Object[]{
					CheckScannerConstantMapper.CHK_CL_MONO.getContantNumber(), CheckScannerConstantMapper.CHK_CL_MONO.getConstant(),
					CheckScannerConstantMapper.CHK_CL_GRAYSCALE.getContantNumber(), CheckScannerConstantMapper.CHK_CL_GRAYSCALE.getConstant(),
					CheckScannerConstantMapper.CHK_CL_16.getContantNumber(), CheckScannerConstantMapper.CHK_CL_16.getConstant(),
					CheckScannerConstantMapper.CHK_CL_256.getContantNumber(), CheckScannerConstantMapper.CHK_CL_256.getConstant(),
					CheckScannerConstantMapper.CHK_CL_FULL.getContantNumber(), CheckScannerConstantMapper.CHK_CL_FULL.getConstant()
			};
		}
	}

	private void setUpColor() {
		color.getItems().clear();
		color.getItems().add(CheckScannerConstantMapper.CHK_CL_16.getConstant());
		color.getItems().add(CheckScannerConstantMapper.CHK_CL_256.getConstant());
		color.getItems().add(CheckScannerConstantMapper.CHK_CL_FULL.getConstant());
		color.getItems().add(CheckScannerConstantMapper.CHK_CL_GRAYSCALE.getConstant());
		color.getItems().add(CheckScannerConstantMapper.CHK_CL_MONO.getConstant());
		try {
			color.setValue(new ColorCodeMapper().getName(((CheckScanner)service).getColor()));
		} catch (JposException e) {
			color.setValue(new ColorCodeMapper().getName(CheckScannerConst.CHK_CL_MONO));
		}
	}

	private void setUpConcurrentMICR() {
		concurrentMICR.getItems().clear();
		concurrentMICR.getItems().add(true);
		concurrentMICR.getItems().add(false);
		try {
			concurrentMICR.setValue(((CheckScanner)service).getConcurrentMICR());
		} catch (JposException e) {
			concurrentMICR.setValue(true);
		}
	}

	class ImageFormatCodeMapper extends ErrorCodeMapper {
		ImageFormatCodeMapper() {
			Mappings = new Object[]{
					CheckScannerConstantMapper.CHK_IF_BMP.getContantNumber(),CheckScannerConstantMapper.CHK_IF_BMP.getConstant(),
					CheckScannerConstantMapper.CHK_IF_GIF.getContantNumber(),CheckScannerConstantMapper.CHK_IF_GIF.getConstant(),
					CheckScannerConstantMapper.CHK_IF_JPEG.getContantNumber(),CheckScannerConstantMapper.CHK_IF_JPEG.getConstant(),
					CheckScannerConstantMapper.CHK_IF_NATIVE.getContantNumber(),CheckScannerConstantMapper.CHK_IF_NATIVE.getConstant(),
					CheckScannerConstantMapper.CHK_IF_TIFF.getContantNumber(),CheckScannerConstantMapper.CHK_IF_TIFF.getConstant()
			};
		}
	}

	private void setUpImageFormat() {
		imageFormat.getItems().clear();
		imageFormat.getItems().add(CheckScannerConstantMapper.CHK_IF_BMP.getConstant());
		imageFormat.getItems().add(CheckScannerConstantMapper.CHK_IF_GIF.getConstant());
		imageFormat.getItems().add(CheckScannerConstantMapper.CHK_IF_JPEG.getConstant());
		imageFormat.getItems().add(CheckScannerConstantMapper.CHK_IF_NATIVE.getConstant());
		imageFormat.getItems().add(CheckScannerConstantMapper.CHK_IF_TIFF.getConstant());
		try {
			imageFormat.setValue(new ImageFormatCodeMapper().getName(((CheckScanner)service).getImageFormat()));
		} catch (JposException e) {
			imageFormat.setValue(new ImageFormatCodeMapper().getName(CheckScannerConst.CHK_IF_TIFF));
		}
	}

	class MapModeCodeMapper extends ErrorCodeMapper {
		MapModeCodeMapper(){
			Mappings = new Object[]{
					CheckScannerConstantMapper.CHK_MM_DOTS.getContantNumber(), CheckScannerConstantMapper.CHK_MM_DOTS.getConstant(),
					CheckScannerConstantMapper.CHK_MM_ENGLISH.getContantNumber(), CheckScannerConstantMapper.CHK_MM_ENGLISH.getConstant(),
					CheckScannerConstantMapper.CHK_MM_METRIC.getContantNumber(), CheckScannerConstantMapper.CHK_MM_METRIC.getConstant(),
					CheckScannerConstantMapper.CHK_MM_TWIPS.getContantNumber(), CheckScannerConstantMapper.CHK_MM_TWIPS.getConstant()
			};
		}
	}

	private void setUpMapMode() {
		mapMode.getItems().clear();
		mapMode.getItems().add(CheckScannerConstantMapper.CHK_MM_DOTS.getConstant());
		mapMode.getItems().add(CheckScannerConstantMapper.CHK_MM_ENGLISH.getConstant());
		mapMode.getItems().add(CheckScannerConstantMapper.CHK_MM_METRIC.getConstant());
		mapMode.getItems().add(CheckScannerConstantMapper.CHK_MM_TWIPS.getConstant());
		try {
			mapMode.setValue(new MapModeCodeMapper().getName(((CheckScanner)service).getMapMode()));
		} catch (JposException e) {
			mapMode.setValue(new MapModeCodeMapper().getName(CheckScannerConst.CHK_MM_ENGLISH));
		}
	}

	private void setUpQuality() {
		quality.getItems().clear();
		try {
			String[] qualities = ((CheckScanner) service).getQualityList().split(",");
			for (int i = 0; i < qualities.length; i++) {
				quality.getItems().add(Integer.parseInt(qualities[i]));
			}
			quality.setValue(((CheckScanner)service).getQuality());
		} catch (JposException e) {}
	}

	private void setUpClearImageBy() {
		clearImage_by.getItems().clear();
		clearImage_by.getItems().add(CheckScannerConstantMapper.CHK_CLR_ALL.getConstant());
		clearImage_by.getItems().add(CheckScannerConstantMapper.CHK_CLR_BY_FILEID.getConstant());
		clearImage_by.getItems().add(CheckScannerConstantMapper.CHK_CLR_BY_FILEINDEX.getConstant());
		clearImage_by.getItems().add(CheckScannerConstantMapper.CHK_CLR_BY_IMAGETAGDATA.getConstant());
		clearImage_by.setValue(CheckScannerConstantMapper.CHK_CLR_ALL.getConstant());
	}

	private void setUpRetrieveImageCropAreaID() {
		retrieveImage_cropAreaID.getItems().clear();
		retrieveImage_cropAreaID.getItems().add(
				CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE.getConstant());
		retrieveImage_cropAreaID.getItems().add(
				CheckScannerConstantMapper.CHK_CROP_AREA_RESET_ALL.getConstant());
		retrieveImage_cropAreaID
				.setValue(CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE.getConstant());
	}

	private void setUpDefineCropAreaCropAreaID() {
		defineCropArea_cropAreaID.getItems().clear();
		defineCropArea_cropAreaID.getItems().add(
				CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE.getConstant());
		defineCropArea_cropAreaID.getItems().add(
				CheckScannerConstantMapper.CHK_CROP_AREA_RESET_ALL.getConstant());
		defineCropArea_cropAreaID.setValue(CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE
				.getConstant());
	}

	private void setUpRetrieveMemoryBy() {
		retrieveMemory_by.getItems().clear();
		retrieveMemory_by.getItems().add(CheckScannerConstantMapper.CHK_LOCATE_BY_FILEID.getConstant());
		retrieveMemory_by.getItems().add(CheckScannerConstantMapper.CHK_LOCATE_BY_FILEINDEX.getConstant());
		retrieveMemory_by.getItems().add(CheckScannerConstantMapper.CHK_LOCATE_BY_IMAGETAGDATA.getConstant());
		retrieveMemory_by.setValue(CheckScannerConstantMapper.CHK_LOCATE_BY_FILEID.getConstant());

	}

	private void setUpStoreImageCropAreaID() {
		storeImage_cropAreaID.getItems().clear();
		storeImage_cropAreaID.getItems().add(
				CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE.getConstant());
		storeImage_cropAreaID.getItems()
				.add(CheckScannerConstantMapper.CHK_CROP_AREA_RESET_ALL.getConstant());
		storeImage_cropAreaID.setValue(CheckScannerConstantMapper.CHK_CROP_AREA_ENTIRE_IMAGE.getConstant());

	}

	private void setUpDefineCropAreaCX() {
		defineCropArea_cx.getItems().clear();
		defineCropArea_cx.getItems().add(CheckScannerConstantMapper.CHK_CROP_AREA_RIGHT.getConstant());
		defineCropArea_cx.setValue(CheckScannerConstantMapper.CHK_CROP_AREA_RIGHT.getConstant());
	}

	private void setUpDefineCropAreaCY() {
		defineCropArea_cy.getItems().clear();
		defineCropArea_cy.getItems().add(CheckScannerConstantMapper.CHK_CROP_AREA_BOTTOM.getConstant());
		defineCropArea_cy.setValue(CheckScannerConstantMapper.CHK_CROP_AREA_BOTTOM.getConstant());
	}

	@Override
	public void setupGuiObjects() {
		super.setupGuiObjects();
		setUpColor();
		setUpConcurrentMICR();
		setUpImageFormat();
		setUpMapMode();
		setUpQuality();
		setUpClearImageBy();
		setUpRetrieveImageCropAreaID();
		setUpDefineCropAreaCropAreaID();
		setUpRetrieveMemoryBy();
		setUpStoreImageCropAreaID();
		setUpDefineCropAreaCX();
		setUpDefineCropAreaCY();
		setDocumentHeight();
		setDocumentWidth();
		setFileID();
		setFileIndex();
		setContrast();
	}


	private void setDocumentHeight() {
		try {
			documentHeight.setText(Integer.toString(((CheckScanner)service).getDocumentHeight()));
		} catch (JposException e) {
			documentHeight.setText("");
		}
	}

	private void setDocumentWidth() {
		try {
			documentWidth.setText(Integer.toString(((CheckScanner)service).getDocumentWidth()));
		} catch (JposException e) {
			documentWidth.setText("");
		}
	}

	private void setFileID() {
		try {
			fileIndex.setText(((CheckScanner)service).getFileID());
		} catch (JposException e) {
			fileIndex.setText("");
		}
	}

	private void setFileIndex() {
		try {
			fileIndex.setText(Integer.toString(((CheckScanner)service).getFileIndex()));
		} catch (JposException e) {
			fileIndex.setText("0");
		}
	}

	private void setContrast() {
		try {
			contrast.setValue(((CheckScanner)service).getContrast());
		} catch (JposException e) {
			contrast.setValue(50);
		}
	}
}