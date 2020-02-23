package postest2;

public class BillDispenserConstantMapper extends CommonConstantMapper {

	// ///////////////////////////////////////////////////////////////////
	// "DeviceStatus" Property Constants
	// ///////////////////////////////////////////////////////////////////
	@BelongingProperty(PropertyNames.getDeviceStatus)
	public static final ConstantConverter BDSP_STATUS_OK = new ConstantConverter(0, "STATUS_OK");
	@BelongingProperty(PropertyNames.getDeviceStatus)
	public static final ConstantConverter BDSP_STATUS_EMPTY = new ConstantConverter(11, "STATUS_EMPTY");
	@BelongingProperty(PropertyNames.getDeviceStatus)
	public static final ConstantConverter BDSP_STATUS_NEAREMPTY = new ConstantConverter(12,
			"STATUS_NEAREMPTY");
	@BelongingProperty(PropertyNames.getDeviceStatus)
	public static final ConstantConverter BDSP_STATUS_JAM = new ConstantConverter(31, "STATUS_JAM");

	public static int getConstantNumberFromString(String constant) {

		if (BDSP_STATUS_OK.getConstant().equals(constant)) {
			return BDSP_STATUS_OK.getContantNumber();
		}

		if (BDSP_STATUS_EMPTY.getConstant().equals(constant)) {
			return BDSP_STATUS_EMPTY.getContantNumber();
		}

		if (BDSP_STATUS_NEAREMPTY.getConstant().equals(constant)) {
			return BDSP_STATUS_NEAREMPTY.getContantNumber();
		}

		if (BDSP_STATUS_JAM.getConstant().equals(constant)) {
			return BDSP_STATUS_JAM.getContantNumber();
		}

		return CommonConstantMapper.getConstantNumberFromString(constant);
	}
}
