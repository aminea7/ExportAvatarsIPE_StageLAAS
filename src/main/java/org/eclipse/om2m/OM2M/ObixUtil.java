package org.eclipse.om2m.OM2M;


//import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.ShortName;
import org.eclipse.om2m.commons.obix.Contract;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Op;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.Uri;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;
//import org.eclipse.om2m.ipe.sample.constants.Operations;
//import org.eclipse.om2m.ipe.sample.constants.SampleConstants;
//import org.eclipse.om2m.ipe.sample.model.Lamp;

public class ObixUtil {
	
	/**
	 * Returns an obix XML representation describing the lamp.
	 * @param cseId - SclBase id
	 * @param appId - Application Id
	 * @param stateCont - the STATE container id
	 * @return Obix XML representation
	 */
	public static String getDescriptorRep(String cseId, String appId, String stateCont) {
		//String prefix = cseId+"/"+ Constants.CSE_NAME + "/" + appId;
		String prefix = "http://localhost:8080/~/mn-cse/mn-name/Repository1";
		//System.out.println("[IPE AVATARS] PREFIX: "+prefix);
		// oBIX
		Obj obj = new Obj();
		obj.add(new Str("type","Lamp"));
		obj.add(new Str("location","Home"));
		obj.add(new Str("appId",appId));
		// OP GetState from SCL DataBase
		Op opState = new Op();
		opState.setName("getState");
		opState.setHref(new Uri(prefix  +"/"+stateCont+"/"+ ShortName.LATEST));
		opState.setIs(new Contract("retrieve"));
		opState.setIn(new Contract("obix:Nil"));
		opState.setOut(new Contract("obix:Nil"));
		obj.add(opState);
		// OP GetState from SCL IPU
		Op opStateDirect = new Op();
		opStateDirect.setName("getState(Direct)");
		opStateDirect.setHref(new Uri(prefix + "?op=getStateDirect&lampid=" + appId));
		opStateDirect.setIs(new Contract("execute"));
		opStateDirect.setIn(new Contract("obix:Nil"));
		opStateDirect.setOut(new Contract("obix:Nil"));
		obj.add(opStateDirect);
		// OP SwitchON
		Op opON = new Op();
		opON.setName("switchON");
		//opON.setHref(new Uri(prefix + "?op=setOn&lampid=" + appId));
		opON.setHref(new Uri("http://localhost:8080/~/mn-cse/mn-name/Repository1?op=setOn&lampid=2"));
		opON.setIs(new Contract("execute"));
		opON.setIn(new Contract("obix:Nil"));
		opON.setOut(new Contract("obix:Nil"));
		obj.add(opON);
		// OP SwitchOFF
		Op opOFF = new Op();
		opOFF.setName("switchOFF");
		opOFF.setHref(new Uri(prefix  + "?op=setOff&lampid=" + appId));
		opOFF.setIs(new Contract("execute"));
		opOFF.setIn(new Contract("obix:Nil"));
		opOFF.setOut(new Contract("obix:Nil"));
		obj.add(opOFF);
		// OP Toggle
		Op opToggle = new Op();
		opToggle.setName("toggle");
		opToggle.setHref(new Uri(prefix + "?op=toggle&lampid=" + appId));
		opToggle.setIs(new Contract("execute"));
		opToggle.setIn(new Contract("obix:Nil"));
		opToggle.setOut(new Contract("obix:Nil"));
		obj.add(opToggle);

		return ObixEncoder.toString(obj);
	}

	/**
	 * Returns an obix XML representation describing the current state.
	 * @param lampId - Application Id
	 * @param value - current lamp state
	 * @return Obix XML representation
	 */
	
	/*
	public static String getStateRep(String lampId, boolean value) {
		// oBIX
		Obj obj = new Obj();
		obj.add(new Str("type",Lamp.TYPE));
		obj.add(new Str("location",Lamp.LOCATION));
		obj.add(new Str("lampId",lampId));
		obj.add(new Bool("state",value));
		return ObixEncoder.toString(obj);
	}

	public static String createLampAllDescriptor(){
		String prefix = SampleConstants.CSE_ID +"/"+ Constants.CSE_NAME + "/" + "LAMP_ALL";
		Obj descriptor = new Obj();
		Op opSwitchOn = new Op();
		opSwitchOn.setName(Operations.SET_ON.toString());
		opSwitchOn.setHref(prefix + "?op="+ Operations.ALL_ON);
		opSwitchOn.setIs(new Contract("execute"));
		descriptor.add(opSwitchOn);

		Op opSwitchOff = new Op();
		opSwitchOff.setName(Operations.SET_OFF.toString());
		opSwitchOff.setHref(prefix + "?op=" + Operations.ALL_OFF);
		opSwitchOff.setIs(new Contract("execute"));
		descriptor.add(opSwitchOff);

		Op opToggleAll = new Op();
		opToggleAll.setName(Operations.ALL_TOGGLE.toString());
		opToggleAll.setHref(prefix + "?op=" + Operations.ALL_TOGGLE);
		opToggleAll.setIs(new Contract("execute"));
		descriptor.add(opToggleAll);
		return ObixEncoder.toString(descriptor);
	}*/
	
}
