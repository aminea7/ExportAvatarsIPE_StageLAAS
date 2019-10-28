package org.eclipse.om2m.Avatars;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.interworking.service.InterworkingService;
//import org.eclipse.om2m.ipe.sample.constants.Operations;
//import org.eclipse.om2m.ipe.sample.constants.SampleConstants;
//import org.eclipse.om2m.ipe.sample.controller.SampleController;

public class SampleRouter implements InterworkingService{

	private static Log LOGGER = LogFactory.getLog(SampleRouter.class);

	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		System.out.println("SAMPLE ROUTER of IPE AVATAR");

		ResponsePrimitive response = new ResponsePrimitive(request);
		if(request.getQueryStrings().containsKey("op")){
			String operation = request.getQueryStrings().get("op").get(0);
			System.out.println("SAMPLE ROUTER of IPE AVATAR");
			//Operations op = Operations.getOperationFromString(operation);
			String avatar= null;
			if(request.getQueryStrings().containsKey("avatar")){
				avatar = request.getQueryStrings().get("avatar").get(0);
			}
			LOGGER.info("Received request in Sample IPE: op=" + operation + " ; avatar=" + avatar);
			System.out.println("SAMPLE ROUTER OPERATION for IPE Avatars");
			//opON.setHref(new Uri(prefix + "?op=setOn&lampid=" + appId));


			switch(operation){
			case "setOn":
				//SampleController.setLampState(lampid, true);
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("setOn");
				break;
			case "setOff":
				//SampleController.setLampState(lampid, false);
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("setOff");
				break;
			case "TOGGLE":
				//SampleController.toggleLamp(lampid);
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("toggle");

				break;
			case "ALL_ON":
				//SampleController.setAllOn();
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("ALL_ON");

				break;
			case "ALL_OFF":
				//SampleController.setAllOff();
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("ALL_OFF");
				break;
			case "ALL_TOGGLE":
				//SampleController.toogleAll();
            	//response.setResponseStatusCode(ResponseStatusCode.OK);
				System.out.println("ALL_TOOGLE");
				break;
			case "GET_STATE":
				// Shall not get there...
				throw new BadRequestException();
			case "GET_STATE_DIRECT":
				//String content = SampleController.getFormatedLampState(lampid);
				//response.setContent(content);
				//request.setReturnContentType(MimeMediaType.OBIX);
				//response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			default:
				throw new BadRequestException();
			}
		}
		if(response.getResponseStatusCode() == null){
			response.setResponseStatusCode(ResponseStatusCode.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public String getAPOCPath() {
		System.out.println("[IPE AVATARS] GET APOCPATH: ");
		return "Avatars_poa";
		//return null;
	}
	
}
