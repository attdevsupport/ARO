package com.att.aro.json;

import java.util.ArrayList;
import java.util.List;

import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData;

public class RRCStateGenerator {
	
	public RRCStateGenerator() {}
	
	private double idleTime;
	private double dchTime;
	private double dchTailTime;
	private double fachTime;
	private double fachTailTime;

	private double lteIdleTime;
	private double lteIdleToCRPromotionTime;
	private double lteCrTime;
	private double lteCrTailTime;
	private double lteDrxShortTime;
	private double lteDrxLongTime;

	private int idleToDch;
	private int fachToDch;

	private double idleToDchTime;
	private double fachToDchTime;

	private double idleEnergy;
	private double dchEnergy;
	private double fachEnergy;
	private double idleToDchEnergy;
	private double fachToDchEnergy;
	private double dchTailEnergy;
	private double fachTailEnergy;
	private double totalRRCEnergy;
	private double joulesPerKilobyte;

	private double lteIdleEnergy;
	private double lteIdleToCRPromotionEnergy;
	private double lteCrEnergy;
	private double lteCrTailEnergy;
	private double lteDrxShortEnergy;
	private double lteDrxLongEnergy;

	private double crPower;
	
	private double wifiActiveEnergy;
	private double wifiTailEnergy;
	private double wifiIdleEnergy;
    
	private double wifiActiveTime;
	private double wifiTailTime;
	private double wifiIdleTime;
	
	RRCStateSimulator rrcSimulator = new RRCStateSimulator();
	
	public void refresh(TraceData.Analysis analysis){
		RRCStateMachine rrc = analysis.getRrcStateMachine();
		idleTime = rrc.getIdleTime();
		dchTime = rrc.getDchTime();
		dchTailTime = rrc.getDchTailTime();
		fachTime = rrc.getFachTime();		
		fachTailTime = rrc.getFachTailTime();
		
		lteIdleTime = rrc.getLteIdleTime();
		lteIdleToCRPromotionTime = rrc.getLteIdleToCRPromotionTime();
		lteCrTime = rrc.getLteCrTime();
		lteCrTailTime = rrc.getLteCrTailTime();
		lteDrxShortTime = rrc.getLteDrxShortTime();
		lteDrxLongTime = rrc.getLteDrxLongTime();
		
		idleToDch = rrc.getIdleToDchCount();
		fachToDch = rrc.getFachToDchCount();
		
		idleToDchTime = rrc.getIdleToDchTime();
		fachToDchTime = rrc.getFachToDchTime();
		
		idleEnergy = rrc.getIdleEnergy();
		dchEnergy = rrc.getDchEnergy();
		fachEnergy = rrc.getFachEnergy();
		idleToDchEnergy = rrc.getIdleToDchEnergy();
		fachToDchEnergy = rrc.getFachToDchEnergy();
		dchTailEnergy = rrc.getDchTailEnergy();
		fachTailEnergy = rrc.getFachTailEnergy();
		totalRRCEnergy = rrc.getTotalRRCEnergy();
		joulesPerKilobyte = rrc.getJoulesPerKilobyte();
		
		lteIdleEnergy = rrc.getLteIdleEnergy();
		lteIdleToCRPromotionEnergy = rrc.getLteIdleToCRPromotionEnergy();
		lteCrEnergy = rrc.getLteCrEnergy();
		lteCrTailEnergy = rrc.getLteCrTailEnergy();
		lteDrxShortEnergy = rrc.getLteDrxShortEnergy();
		lteDrxLongTime = rrc.getLteDrxLongEnergy();
		
		crPower = rrc.getCrPower();
		
		wifiActiveEnergy = rrc.getWifiActiveEnergy();
		wifiTailEnergy = rrc.getWifiIdleEnergy();
		wifiIdleEnergy = rrc.getWifiTailEnergy();
		
		wifiActiveTime = rrc.getWifiActiveTime();
		wifiTailTime = rrc.getWifiTailTime();
		wifiIdleTime = rrc.getWifiIdleTime();

	}
	
	public RRCStateSimulator getRRCStateDetails(){
		RRCStateSimulator rrcSimulator = new RRCStateSimulator();
		
		List<RRCStateDetails> rrcstateg = new ArrayList<RRCStateDetails>();
		
		RRCStateDetails idlestate = new RRCStateDetails();
		idlestate.setName("idle");
		idlestate.setTime(idleTime);
		idlestate.setEnergy(idleEnergy);
		rrcstateg.add(idlestate);
		
		RRCStateDetails dchstate = new RRCStateDetails();
		dchstate.setName("dch");
		dchstate.setTime(dchTime);
		dchstate.setEnergy(dchEnergy);
		rrcstateg.add(dchstate);
		
		RRCStateDetails dchTailstate = new RRCStateDetails();
		dchTailstate.setName("dchTail");
		dchTailstate.setTime(dchTailTime);
		dchTailstate.setEnergy(dchTailEnergy);
		rrcstateg.add(dchTailstate);
		
		RRCStateDetails fachstate = new RRCStateDetails();
		fachstate.setName("fach");
		fachstate.setTime(fachTime);
		fachstate.setEnergy(fachEnergy);
		rrcstateg.add(fachstate);
		
		RRCStateDetails fachTailstate = new RRCStateDetails();
		fachTailstate.setName("fachTail");
		fachTailstate.setTime(fachTailTime);
		fachTailstate.setEnergy(fachTailEnergy);
		rrcstateg.add(fachTailstate);
		
		RRCStateDetails idleToDchState = new RRCStateDetails();
		idleToDchState.setName("idleToDch");
		idleToDchState.setTime(idleToDchTime);
		idleToDchState.setEnergy(idleToDchEnergy);
		rrcstateg.add(idleToDchState);
		
		RRCStateDetails fachToDchState = new RRCStateDetails();
		fachToDchState.setName("fachToDch");
		fachToDchState.setTime(fachToDchTime);
		fachToDchState.setEnergy(fachToDchEnergy);
		rrcstateg.add(fachToDchState);
		
		rrcSimulator.setRccstateg(rrcstateg.toArray(new RRCStateDetails[rrcstateg.size()]));
		
		List<RRCStateDetails> rrcstatelte = new ArrayList<RRCStateDetails>();
		
		RRCStateDetails lteIdle = new RRCStateDetails();
		lteIdle.setName("lteIdle");
		lteIdle.setTime(lteIdleTime);
		lteIdle.setEnergy(lteIdleEnergy);
		rrcstatelte.add(lteIdle);
		
		RRCStateDetails lteIdleToCRPromotion = new RRCStateDetails();
		lteIdleToCRPromotion.setName("lteIdleToCRPromotion");
		lteIdleToCRPromotion.setTime(lteIdleToCRPromotionTime);
		lteIdleToCRPromotion.setEnergy(lteIdleToCRPromotionEnergy);
		rrcstatelte.add(lteIdleToCRPromotion);
		
		RRCStateDetails lteCr = new RRCStateDetails();
		lteCr.setName("lteCr");
		lteCr.setTime(lteCrTime);
		lteCr.setEnergy(lteCrEnergy);
		rrcstatelte.add(lteCr);
		
		RRCStateDetails lteCrTail = new RRCStateDetails();
		lteCrTail.setName("lteCrTail");
		lteCrTail.setTime(lteCrTailTime);
		lteCrTail.setEnergy(lteCrTailEnergy);
		rrcstatelte.add(lteCrTail);
		
		RRCStateDetails lteDrxShort = new RRCStateDetails();
		lteDrxShort.setName("lteDrxShort");
		lteDrxShort.setTime(lteDrxShortTime);
		lteDrxShort.setEnergy(lteDrxShortEnergy);
		rrcstatelte.add(lteDrxShort);
		
		RRCStateDetails lteDrxLong = new RRCStateDetails();
		lteDrxLong.setName("lteDrxLong");
		lteDrxLong.setTime(lteDrxLongTime);
		lteDrxLong.setEnergy(lteDrxLongEnergy);
		rrcstatelte.add(lteDrxLong);
		
		rrcSimulator.setRccstatelte(rrcstatelte.toArray(new RRCStateDetails[rrcstatelte.size()]));
		
		List<RRCStateDetails> rrcstatewifi = new ArrayList<RRCStateDetails>();
		
		RRCStateDetails wifiIdle = new RRCStateDetails();
		wifiIdle.setName("wifiIdle");
		wifiIdle.setTime(wifiIdleTime);
		wifiIdle.setEnergy(wifiIdleEnergy);
		rrcstatewifi.add(wifiIdle);
		
		RRCStateDetails wifiTail = new RRCStateDetails();
		wifiTail.setName("wifiTail");
		wifiTail.setTime(wifiTailTime);
		wifiTail.setEnergy(wifiTailEnergy);
		rrcstatewifi.add(wifiTail);
		
		RRCStateDetails wifiActive = new RRCStateDetails();
		wifiActive.setName("wifiActive");
		wifiActive.setTime(wifiActiveTime);
		wifiActive.setEnergy(wifiActiveEnergy);
		rrcstatewifi.add(wifiActive);
		
		rrcSimulator.setRccstatewifi(rrcstatewifi.toArray(new RRCStateDetails[rrcstatewifi.size()]));

		rrcSimulator.setCrPower(crPower);
		rrcSimulator.setFachToDch(fachToDch);
		rrcSimulator.setIdleToDch(idleToDch);
		rrcSimulator.setTotalRRCEnergy(totalRRCEnergy);
		rrcSimulator.setJoulesPerKilobyte(joulesPerKilobyte);		
		
		
		return rrcSimulator;
	}

}
