package service.certification_wx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.common.bean.JsonMessage;
import service.common.bean.JsonRequest;
import service.common.bean.JsonResponse;
import service.communication.EmbedBSP;
import service.communication.IBSPConstant;
import service.communication.InterfaceCodeNames;
import service.communication.TransCodeConstant;
import service.medicalCommon.SerialGeneraterUtil;
import both.common.util.DataUtil;
import both.common.util.DateUtil;
import both.common.util.LoggerUtil;
import both.common.util.StringUtilEx;
import both.constants.IResponseConstant;
import both.constants.LoginConstant;
import both.constants.MedicalConstant;
import cn.com.bankit.phoenix.commons.util.StringUtil;
import cn.com.bankit.phoenix.jdbc.tool.DBAccessor;

/**
 * 实名认证
 *
 */
public class FaceRecognition {

	/**
	 * 
	 */
	public FaceRecognition() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 根据身份证姓名,身份证号,人脸图片base64转码
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonResponse idNamePhotoCheckWX(JsonRequest request) throws Exception{
		
		JsonResponse response = new JsonResponse();
		//获得用户OPENID
		String OPENID = StringUtilEx.convertNullToEmpty(((Map<String,Object>) request.getHeader("tellerInfo")).get("OPENID"));
		
		//留痕map
		Map<String, Object> insMap = new HashMap<String, Object>();

		//姓名
		String name = (String) request.getAsString("name");
		//手机号
		String phone = (String) request.getAsString("phone");
		
		//身份证号
		String idCard = (String) request.getAsString("idCard");
		
		//人脸图像
		String image = (String) request.getAsString("image");
			
		Map<String, Object> body = new HashMap<String, Object>();
		////////////////////////test-status///////////////
		insMap.put("OPENID", OPENID);
    	insMap.put("serial", SerialGeneraterUtil.createVal("v"));//流水号
    	insMap.put("card_name", name);//身份证姓名
    	insMap.put("card_number", idCard);//身份证号码
    	insMap.put("validate_time", DateUtil.getNowDate());//校验日期
    	insMap.put("validate_code", MedicalConstant.INIT_CODE);//初始为0
    	insMap.put("validate_msg", ""); //返回存放异常信息
    	insMap.put("isUser", StringUtilEx.convertNullToEmpty(request.getAsString("isUser")));
    	
    	insMap.put("user_phone",  phone);
    	insMap.put("sex", request.getAsString("sex"));
    	insMap.put("age", request.getAsString("age"));
    	insMap.put("birthday", request.getAsString("birthday"));
    	insMap.put("guid", "20190711110455_937dG995_1880387");//认证结果流水，无需传参
		installRegisterOrder(insMap);
		/////////////////////////test-end//////////////////
		//调用内嵌BSP
       /* try{
        	//查询实名认证参数
        	String sql = "select para_value1,para_value2,para_value3  from cmb_medical_sys_config where para_type = 'OCR' and para_no = 'OCR002'";
        	
        	List<Map<String, Object>> configs = DBAccessor.getDBAccessor().query(sql);
        	//用户名
        	String loginName = String.valueOf(configs.get(0).get("para_value1")) ;
        	//密码
        	String pwd =  String.valueOf(configs.get(0).get("para_value2")) ;
        	//服务名
        	String serviceName =  String.valueOf(configs.get(0).get("para_value3")) ;
        	//交易码
        	String TransCode  = TransCodeConstant.OCR002;
        	//发送流水号
        	String mvTrackId = SerialGeneraterUtil.generaterFaceSerial(serviceName, loginName);
        	
        	//留痕map组装
        	insMap.put("OPENID", OPENID);
        	insMap.put("serial", SerialGeneraterUtil.createVal("v"));//流水号
        	insMap.put("card_name", name);//身份证姓名
        	insMap.put("card_number", idCard);//身份证号码
        	insMap.put("validate_time", DateUtil.getNowDate());//校验日期
        	insMap.put("validate_code", MedicalConstant.INIT_CODE);//初始为0
        	insMap.put("validate_msg", ""); //返回存放异常信息
        	insMap.put("isUser", StringUtilEx.convertNullToEmpty(request.getAsString("isUser")));
        	
        	insMap.put("user_phone",  StringUtilEx.convertNullToEmpty(request.getAsString("user_phone")));
        	insMap.put("sex", request.getAsString("sex"));
        	insMap.put("age", request.getAsString("age"));
        	insMap.put("birthday", request.getAsString("birthday"));

        	
        	body.put("TransCode", TransCode);
        	body.put("mvTrackId", mvTrackId);
        	body.put("loginName", loginName);
        	body.put("pwd",pwd);
        	body.put("serviceName", serviceName);
        	body.put("name", name);
        	body.put("idCard", idCard);
        	body.put("image", image);
        	
        	
        	Map<String, Object> result = new HashMap<String, Object>();
        	result.put("resultCode", "000000");
        	//LoggerUtil.debug("BSP请求数据========="+body.toString());
        	Map<String, Object> result = new EmbedBSP().invoke(
					String.valueOf(System.currentTimeMillis()),
					IBSPConstant.bipMoudleName,IBSPConstant.ocrMoudle,InterfaceCodeNames.OCR002,null,body);
        	
			LoggerUtil.info("BSP返回数据========="+result.toString());
			//人脸识别特殊处理
			if ("1001".equals(result.get(MedicalConstant.SUCCESS_CODE))) {
				insMap.put("guid", result.get("guid"));
				installRegisterOrder(insMap);
				LoggerUtil.error("人脸识别调用成功");
				//response.put("result", result);
				insMap.put("is_realname", "1");
				if(!"".equals(idCard) && idCard.length() > 8){
					idCard = idCard.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
					insMap.put("card_number", idCard);
				}
				response.put("userInfo", insMap);
				response.put(IResponseConstant.retCode, IResponseConstant.SUCCESS);
				response.put(IResponseConstant.retMsg, "实名认证成功");
			} else {
				insMap.put("validate_code", result.get(MedicalConstant.SUCCESS_CODE));
				insMap.put("validate_msg", result.get("MESSAGE"));
				insMap.put("guid", result.get("guid"));
				String errorMsg = exceptionMsg(result.get(MedicalConstant.SUCCESS_CODE),result.get("resultMsg"));
				LoggerUtil.error("认证失败:"+errorMsg);
				response.put(IResponseConstant.retCode, IResponseConstant.FAILED);
				response.put(IResponseConstant.retMsg, errorMsg);
			}
        }catch(Exception e){
        	LoggerUtil.error(e.getMessage());
        	response.put(IResponseConstant.retCode, IResponseConstant.FAILED);
			response.put(IResponseConstant.retMsg, "服务调用失败");
        }*/
		
        return response;

		
	}
	
	/**
	 * 实名认证留痕处理
	 * @throws Exception 
	 */
	public boolean installRegisterOrder(Map<String, Object> requesMap) throws Exception{
		List<String> sqlList = new ArrayList<String>();
		List<Map<String, Object>> pMapList = new ArrayList<Map<String,Object>>();
		LoggerUtil.debug("实名认证留痕信息开始========================================");
		/* 1：向微信用户信息表插入身份证号
		 * 2：根据用户手机号和身份证号查询是否为老用户
		 * 3：如果是老用户，检查是否实名认证，没有-更新实名认证，有无需操作
		 * 4：如果是新用户，注册
		 * 
		 */
		String card_number = String.valueOf(requesMap.get("card_number"));
		String OPENID = String.valueOf(requesMap.get("OPENID"));
		String userPhone = String.valueOf(requesMap.get("user_phone"));
		LoggerUtil.debug("requesMap:"+requesMap.toString());
		LoggerUtil.debug("card_number:"+card_number);
		LoggerUtil.debug("OPENID:"+OPENID);
		LoggerUtil.debug("userPhone:"+userPhone);
		//isUser不为空时，认为是用户实名认证
		String isUser = StringUtilEx.convertNullToEmpty(requesMap.get("isUser"));
		LoggerUtil.debug("isUser:"+isUser);
		if(!DataUtil.isEmpty(isUser)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("card_number", card_number);
			map.put("OPENID", OPENID);
			map.put("userPhone", userPhone);
			//更新用户和微信关联关系
			String sql="UPDATE cmb_medical_wx_userinfo SET card_number = :card_number, user_phone = :userPhone WHERE OPENID = :OPENID";
			sqlList.add(sql);
			pMapList.add(map);
			//查找用户信息表是否有该用户
			String selectUser = "select user_id, is_realname from cmb_medical_user_info where status='0' and (card_number=:card_number and user_phone=:userPhone) or user_phone=:userPhone";
			List<Map<String, Object>> user = DBAccessor.getDBAccessor().query(selectUser, map, null);
			String user_id = SerialGeneraterUtil.createVal("reg"); // 生成userid字符串
			if (!DataUtil.isEmpty(user)){
				//如果存在，判断是否实名认证，如果没有更新并实名认证信息
				if (!LoginConstant.REALNAME.equals(String.valueOf(user.get(0).get("is_realname")))) {
					user_id = String.valueOf(user.get(0).get("user_id"));
					String updateUserSql = "update cmb_medical_user_info set card_name = :card_name,card_number = :card_number, card_type = '2', is_realname = '1' where user_id = :userId";
					map.put("userId", user.get(0).get("user_id"));
					map.put("card_name", requesMap.get("card_name"));
					sqlList.add(updateUserSql);
					pMapList.add(map);
					
					String insertPatientSql = "insert into cmb_medical_patient_info(pid, user_id, user_phone, patient_name, patient_number, " +
							"prove_flag, create_time, update_time, sex, age, birthday,status,relationship) VALUES (:pid, :user_id, :user_phone, :card_name, :card_number," +
							"'1', :create_time, :update_time, :sex, :age, :birthday, '0',:relationship)";
					sqlList.add(insertPatientSql);
					pMapList.add(requesMap);
				}
			}else {
				//不存在，实名认证
				requesMap.put("user_id", user_id);
				requesMap.put("pid", SerialGeneraterUtil.createVal("p"));
				requesMap.put("create_time", DateUtil.getNowDate());
				requesMap.put("update_time", DateUtil.getNowDate());
				requesMap.put("relationship", MedicalConstant.RELATIONSHIP_SELF);
				
				String insertPatientSql = "insert into cmb_medical_patient_info(pid, user_id, user_phone, patient_name, patient_number, " +
						"prove_flag, create_time, update_time, sex, age, birthday,status,relationship) VALUES (:pid, :user_id, :user_phone, :card_name, :card_number," +
						"'1', :create_time, :update_time, :sex, :age, :birthday, '0',:relationship)";
				sqlList.add(insertPatientSql);
				pMapList.add(requesMap);
				
				String insertUserSql = "INSERT INTO cmb_medical_user_info (user_id,card_name,card_type,card_number,is_realname,register_time,last_login_time,STATUS) " +
						"VALUES (:user_id,:card_name,'2',:card_number,'1',now(),now(),'0')";
				sqlList.add(insertUserSql);
				pMapList.add(requesMap);
			}
		}
		String insertSql = "insert into cmb_medical_realname_validate(serial, user_id, card_name, card_number, validate_time," +
				"validate_code, validate_msg, guid) VALUES " +
				"(:serial, :user_id, :card_name, :card_number, :validate_time, :validate_code, :validate_msg, :guid)";
		sqlList.add(insertSql);
		pMapList.add(requesMap);
		
		try {
			DBAccessor.getDBAccessor().batchExecInTransaction(sqlList, pMapList);
			LoggerUtil.debug("实名认真留痕信息结束========================================");
			return true;
		} catch (Exception e) {
			LoggerUtil.debug("===========实名认证留痕失败");
			return false;
		}
	}

	
	private String exceptionMsg(Object code,Object msg){
		if("1002".equals(code) || "1003".equals(code) || "2001".equals(code) || "3001".equals(code) || "3002".equals(code)){
			return String.valueOf(msg);
		}else if("4001".equals(code)){
			return "照片验证未通过,请重新拍摄";
		}
		else if("-1001".equals(code)){
			return "姓名不标准,为空或者包含特殊字符";
		}
		else if("-1002".equals(code)){
			return "身份证号不标准,不符合身份证校验规范";
		}
		else if("-1026".equals(code)){
			return "照片大小超过标准,请重新拍摄";
		}else{
			return "请稍后重试";
		}
		
	}
}
