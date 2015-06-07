package com.caijin.I000Wan.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.caijin.I000Wan.dto.JinZuChuanConfirmDTO;
import com.caijin.I000Wan.dto.JqsConfirmDTO;
import com.caijin.I000Wan.dto.LotteryConfirmInfoDTO;
import com.caijin.I000Wan.entity.FootballMatch;
import com.caijin.I000Wan.entity.MemberUser;
import com.caijin.I000Wan.entity.Order;
import com.caijin.I000Wan.entity.OrderDetail;
import com.caijin.I000Wan.entity.Period;
import com.caijin.I000Wan.service.FootballMatchService;
import com.caijin.I000Wan.service.MemberUserService;
import com.caijin.I000Wan.service.OrderDetailService;
import com.caijin.I000Wan.service.OrderService;
import com.caijin.I000Wan.service.PeriodService;
import com.caijin.I000Wan.util.GenerateOrderNoUtil;

/**
 * 订单控制层
 * @author Jeff
 * @since 2014-07-21
 */
@Controller
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderDetailService orderDetailService;
	
	@Autowired
	private PeriodService periodService;
	
	@Autowired
	private FootballMatchService footballMatchService;
	
	@Autowired
	private MemberUserService memberUserService;
	
	/**
	 * 跳转到充值页面
	 * @return
	 */
	@RequestMapping("/recharge")
	public String toRecharge(HttpServletRequest request){
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		if(memberUser == null){
			return "redirect:/user/login";
		}
		return "order/recharge";
	}
	
	/**
	 * 跳转到支付宝页面
	 * @return
	 */
	@RequestMapping("/order/alipay")
	public String toAlipay(HttpServletRequest request){
		String tradeNo=request.getParameter("tradeNo");
		String subject=request.getParameter("subject");
		String totalMoney=request.getParameter("totalMoney");
		
		String seller_email="2088511221216065";//卖家支付宝账号
			
		request.setAttribute("tradeNo", tradeNo);
		request.setAttribute("subject", subject);
		request.setAttribute("totalMoney", totalMoney);
		request.setAttribute("seller_email", seller_email);
		
		return "order/alipay";
	}
	
	/**
	 * 支付宝服务器异步通知页面
	 * @return
	 */
	@RequestMapping("/alipay/notify")
	public String alipayNotify(){
		
		return "order/notify_url";
	}
	
	/**
	 *支付宝页面跳转同步通知页面
	 * @return
	 */
	@RequestMapping("/alipay/return")
	public String alipayReturn(){
		
		return "order/return_url";
	}
	
	/**
	 * 充值
	 */
	@RequestMapping("/order/recharge/ajax_save")
	@ResponseBody
	public Map doRechargeOrder(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("sucess", false);//默认为失败
		
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		try{
			if(memberUser != null){
				Integer totalMoney=0;
				String totalMoneyStr=(String)request.getParameter("totalMoney");
				
				totalMoney=new Integer(totalMoneyStr);
				
				String orderId = GenerateOrderNoUtil.getOrderNumber();
				
				Order order=new Order();
				order.setOrderId(orderId);
				order.setCreateDate(new Date());
				order.setMemberUser(memberUser);
				order.setName("订单充值");
				order.setOrderStatus(Order.ORDER_SUCESS);
				order.setOrderTime(new Date());
				order.setOrderType(Order.RECHARGE_ORDER);
				order.setPayStatus(Order.PAY_STATUS_NO);
				order.setTotalMoney(totalMoney);
				order.setUpdateDate(new Date());
				
				orderService.save(order);
				
				OrderDetail orderDetail=new OrderDetail();
				orderDetail.setCreateDate(new Date());
				orderDetail.setOrder(order);
				orderDetail.setUpdateDate(new Date());
				
				orderDetailService.save(orderDetail);	
				
				request.getSession().setAttribute("tradeOrder", order);
				
				map.put("orderId", order.getId());
				map.put("sucess", true);
				map.put("msg", "下注成功!");
			}else{
				map.put("sucess", false);
				map.put("msg", "未登录!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("sucess", false);
			map.put("msg", "充值失败!");
		}
		return map;
	}
	
	/**
	 * 我的充值记录
	 * @return
	 */
	@RequestMapping("/myrecharge/list")
	public String myRechargeList(HttpServletRequest request,Model model){
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		List<Order> myRechargeList=new ArrayList<Order>();
		myRechargeList=orderService.getMyRechargeList(memberUser.getId());
		model.addAttribute("myRechargeList", myRechargeList);
		return "user/rechargeRecordList";
	}
	
	/**
	 * 胜负彩购买方法
	 * @return
	 */
	@RequestMapping("/order/sfc/ajax_save")
	@ResponseBody
	public Map sfcOrderSave(HttpServletRequest request){
		
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("sucess", false);//默认为失败
		
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		try{
			if(memberUser != null){
				Integer totalMoney=0;
				String name=(String)request.getParameter("name");
				String totalMoneyStr=(String)request.getParameter("totalMoney");
				String lotteryCode=(String)request.getParameter("lotteryCode");
				String buyCaiNumber=(String)request.getParameter("buyCaiNumber");
				String lotteryCountStr = (String)request.getParameter("lotteryCount");
				Integer lotteryCount = 1;
				
				if(lotteryCountStr != null && !lotteryCountStr.equals("")){
					lotteryCount=Integer.parseInt(lotteryCountStr);
				}
				
				Period period=periodService.findByCurrentPeriod(lotteryCode);

				if(totalMoneyStr != null && !"".equals(totalMoneyStr)){
					totalMoney = Integer.parseInt(totalMoneyStr);
				}
				
				String orderId = GenerateOrderNoUtil.getOrderNumber();
				
				Order order=new Order();
				order.setOrderId(orderId);
				order.setCreateDate(new Date());
				order.setMemberUser(memberUser);
				order.setName(name);
				order.setOrderStatus(Order.ORDER_SUCESS);
				order.setOrderTime(new Date());
				order.setOrderType(Order.PROXY_BUY_ORDER);
				order.setPayStatus(Order.PAY_STATUS_NO);
				order.setPeriod(period);
				order.setTotalMoney(totalMoney);
				order.setUpdateDate(new Date());
				order.setLotteryCount(lotteryCount);
				
				orderService.save(order);
				
				OrderDetail orderDetail=new OrderDetail();
				orderDetail.setBuyCaiNumber(buyCaiNumber);
				orderDetail.setCreateDate(new Date());
				orderDetail.setOrder(order);
				orderDetail.setUpdateDate(new Date());
				
				orderDetailService.save(orderDetail);	
				
				request.getSession().setAttribute("tradeOrder", order);
				
				map.put("sucess", true);
				map.put("msg", "下注成功!");
			}else{
				map.put("sucess", false);
				map.put("msg", "未登录!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("sucess", false);
			map.put("msg", "下注失败!");
		}
		return map;

	}
	
	/**
	 * 竞彩足球购买方法
	 * @return
	 */
	@RequestMapping("/order/jczq/ajax_save")
	@ResponseBody
	public Map jczqOrderSave(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("sucess", false);//默认为失败
		
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		try{
			if(memberUser != null){
				
				String name=(String)request.getParameter("name");
				String totalMoneyStr=(String)request.getParameter("totalMoney");
				String lotteryCountStr = (String)request.getParameter("lotteryCount");
				String footballMatchIdStr = (String)request.getParameter("footballMatchId");
				String selectNumber = request.getParameter("selectNumber");
				String selectType = request.getParameter("selectType");
				
				Integer totalMoney=0;
				Integer lotteryCount = 1;//倍数
				Integer footBallMatchId = 0;
				String lotteryType="胜平负/让球";//0为胜平负/让球 1为胜平负 2为让球胜平负
				
				if(lotteryCountStr != null && !lotteryCountStr.equals("")){
					lotteryCount=Integer.parseInt(lotteryCountStr);
				}
				
				if(totalMoneyStr != null && !"".equals(totalMoneyStr)){
					totalMoney = Integer.parseInt(totalMoneyStr);
				}
				
				if(footballMatchIdStr != null && !"".equals(footballMatchIdStr)){
					footBallMatchId = Integer.parseInt(footballMatchIdStr);
				}
				
				if(selectType == "0"){
					lotteryType="胜平负/让球";
				}else if(selectType == "1"){
					lotteryType="胜平负";
				}else if(selectType == "2"){
					lotteryType="让球胜平负";
				}
				
				FootballMatch footBallMatch = footballMatchService.findFootballMatchById(footBallMatchId);
				
				String orderId = GenerateOrderNoUtil.getOrderNumber();
				//String periodId = footBallMatch.getPeriodId();
				//Period period = periodService.findPeriodById(Integer.parseInt(periodId));
				
				name = "竞彩足球-"+footBallMatch.getHomesxName()+"-"+footBallMatch.getAwaysxName()+"-"+selectNumber;
				
				Order order=new Order();
				order.setOrderId(orderId);
				order.setCreateDate(new Date());
				//order.setPeriod(period);
				order.setMemberUser(memberUser);
				order.setName(name);
				order.setOrderStatus(Order.ORDER_SUCESS);
				order.setOrderTime(new Date());
				order.setOrderType(Order.PROXY_BUY_ORDER);
				order.setPayStatus(Order.PAY_STATUS_NO);
				order.setTotalMoney(totalMoney);
				order.setUpdateDate(new Date());
				order.setLotteryCount(1);
				order.setLotteryType(lotteryType);
				
				orderService.save(order);
				
				OrderDetail orderDetail=new OrderDetail();
				orderDetail.setBuyCaiNumber(selectNumber);
				orderDetail.setCreateDate(new Date());
				orderDetail.setOrder(order);
				orderDetail.setUpdateDate(new Date());
				
				orderDetailService.save(orderDetail);	
				
				request.getSession().setAttribute("tradeOrder", order);
				
				map.put("sucess", true);
				map.put("msg", "下注成功!");
			}else{
				map.put("sucess", false);
				map.put("msg", "未登录!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("sucess", false);
			map.put("msg", "下注失败!");
		}
		return map;
	}
	
	/**
	 * 竞彩足球进球数购买方法
	 * @return
	 */
	@RequestMapping("/order/jczq_jqs/ajax_save")
	@ResponseBody
	public Map jczqJqsOrderSave(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("sucess", false);//默认为失败
		
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		try{
			if(memberUser != null){
				
				String name=(String)request.getParameter("name");
				String totalMoneyStr=(String)request.getParameter("totalMoney");
				String lotteryCountStr = (String)request.getParameter("lotteryCount");
				String lotteryInfoListStr = (String)request.getParameter("lotteryInfoListStr");
				
				Integer totalMoney=0;
				Integer lotteryCount = 1;//倍数
				String lotteryType="2串1";
				
				if(lotteryCountStr != null && !lotteryCountStr.equals("")){
					lotteryCount=Integer.parseInt(lotteryCountStr);
				}
				
				if(totalMoneyStr != null && !"".equals(totalMoneyStr)){
					totalMoney = Integer.parseInt(totalMoneyStr);
				}
				String[] selectedLotteryInfoArray = null;
				
				if(lotteryInfoListStr != null && !"".equals(lotteryInfoListStr)){
					lotteryInfoListStr = lotteryInfoListStr.substring(0, lotteryInfoListStr.length()-1);
					selectedLotteryInfoArray = lotteryInfoListStr.split(";");
					
					String orderId = GenerateOrderNoUtil.getOrderNumber();
					//String periodId = footBallMatch.getPeriodId();
					//Period period = periodService.findPeriodById(Integer.parseInt(periodId));
					
					name = "竞彩足球进球数-"+totalMoneyStr+"元";
					
					Order order=new Order();
					order.setOrderId(orderId);
					order.setCreateDate(new Date());
					//order.setPeriod(period);
					order.setMemberUser(memberUser);
					order.setName(name);
					order.setOrderStatus(Order.ORDER_SUCESS);
					order.setOrderTime(new Date());
					order.setOrderType(Order.PROXY_BUY_ORDER);
					order.setPayStatus(Order.PAY_STATUS_NO);
					order.setTotalMoney(totalMoney);
					order.setUpdateDate(new Date());
					order.setLotteryCount(1);
					order.setLotteryType(lotteryType);
					
					orderService.save(order);
					
					for(int i=0;i<selectedLotteryInfoArray.length;i++){
						String[] lotteryArray = selectedLotteryInfoArray[i].split("_");
						OrderDetail orderDetail=new OrderDetail();
						orderDetail.setBuyCaiNumber(lotteryArray[0]);
						orderDetail.setCreateDate(new Date());
						orderDetail.setOrder(order);
						orderDetail.setUpdateDate(new Date());
						
						orderDetailService.save(orderDetail);	
					}
					
					request.getSession().setAttribute("tradeOrder", order);
					
					map.put("sucess", true);
					map.put("msg", "下注成功!");
				}else{
					map.put("sucess", false);
					map.put("msg", "选择参数错误，下注失败!");
				}				
			}else{
				map.put("sucess", false);
				map.put("msg", "未登录!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("sucess", false);
			map.put("msg", "下注失败!");
		}
		return map;
	}
	
	/**
	 * 跳转到订单确认页面
	 * @param request
	 * @return
	 */
	@RequestMapping("/order/sfc/orderconfirm")
	public String toOrderConfirm(HttpServletRequest request,Model model){
		
		String lotteryCount = request.getParameter("lotteryCount");
		String totalMoney = request.getParameter("totalMoney");
		String lotteryCode = request.getParameter("lotteryCode");
		String buyCaiNumber = request.getParameter("buyCaiNumber");
		String name = request.getParameter("name");
		
		Period currentPeriod=periodService.findByCurrentPeriod(Period.FOOTBALL_SFC);
		String[] buyCaiNumberArray=null;
		if(buyCaiNumber != null && !buyCaiNumber.equals("")){
			buyCaiNumberArray = buyCaiNumber.split("-");
		}
		List<LotteryConfirmInfoDTO> confirmInfoList=footballMatchService.getLotteryConfirmInfo(Period.FOOTBALL_SFC, currentPeriod==null?"":currentPeriod.getLotteryPeriod(),buyCaiNumberArray);

		
		model.addAttribute("currentPeriod", currentPeriod);
		model.addAttribute("lotteryCount", lotteryCount);
		model.addAttribute("totalMoney", totalMoney);
		model.addAttribute("lotteryCode", lotteryCode);
		model.addAttribute("name", name);
		model.addAttribute("buyCaiNumber", buyCaiNumber);
		model.addAttribute("confirmInfoList", confirmInfoList);
		
		return "order/orderConfirm";
	}
	
	/**
	 * 跳转到竞彩足球订单确认页面
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/order/jczq/orderconfirm")
	public String toJczqOrderConfirm(HttpServletRequest request,Model model){
		String totalMoneyStr=(String)request.getParameter("totalMoney");
		String lotteryCountStr = (String)request.getParameter("lotteryCount");//倍数
		String lotteryInfoStr = (String)request.getParameter("lotteryInfoStr");
		String selectType = request.getParameter("selectType"); //选择的类型 0为胜平负/让球 1为胜平负 2为让球胜平负
		String[] selectedLotteryInfoArray = null;
		String selectNumber = "";//选择的胜、平、负
		String spfbl = ""; //赔率
		String footballMatchIdStr = ""; //赛事ID
		String betDate = ""; //比赛时间
		String gameNumber = "";
		
		FootballMatch footBallMatch = null;
		
		if(lotteryInfoStr != null && !"".equals(lotteryInfoStr)){
			selectedLotteryInfoArray = lotteryInfoStr.split("_");
			if(selectedLotteryInfoArray != null && selectedLotteryInfoArray.length >0){
				selectNumber = selectedLotteryInfoArray[0];
				spfbl = selectedLotteryInfoArray[1];
				footballMatchIdStr=selectedLotteryInfoArray[2];
				betDate=selectedLotteryInfoArray[3];
				gameNumber=selectedLotteryInfoArray[4];
			}
			footBallMatch=footballMatchService.findFootballMatchById(Integer.parseInt(footballMatchIdStr));
		}
		
		model.addAttribute("totalMoneyStr", totalMoneyStr);
		model.addAttribute("lotteryCountStr", lotteryCountStr);
		model.addAttribute("selectNumber", selectNumber);
		model.addAttribute("spfbl", spfbl);
		model.addAttribute("betDate", betDate);
		model.addAttribute("gameNumber", gameNumber);
		model.addAttribute("footballMatchId", footballMatchIdStr);
		model.addAttribute("footBallMatch", footBallMatch);
		model.addAttribute("selectType", selectType);
		
		return "order/jczqOrderConfirm";
	}
	
	/**
	 * 跳转到竞彩足球进球数订单确认页面
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/order/jczq/jqs/orderconfirm")
	public String toJczqJqsOrderConfirm(HttpServletRequest request,Model model){
		String totalMoneyStr=(String)request.getParameter("totalMoney");
		String lotteryCountStr = (String)request.getParameter("lotteryCount");//倍数
		String lotteryInfoListStr = (String)request.getParameter("lotteryInfoList");

		String[] selectedLotteryInfoArray = null;
		
		List<JqsConfirmDTO> jqsConfirmList = new ArrayList<JqsConfirmDTO>();
		
		
		if(lotteryInfoListStr != null && !"".equals(lotteryInfoListStr)){
			lotteryInfoListStr = lotteryInfoListStr.substring(0, lotteryInfoListStr.length()-1);
			selectedLotteryInfoArray = lotteryInfoListStr.split(";");
			if(selectedLotteryInfoArray != null && selectedLotteryInfoArray.length >0){
				String[] lotteryInfoArray = null;
				for(int i=0;i<selectedLotteryInfoArray.length;i++){
					
					
					JqsConfirmDTO jqsConfirmDTO =new JqsConfirmDTO();
					lotteryInfoArray = selectedLotteryInfoArray[i].split("_");
					jqsConfirmDTO.setLotteryNo(lotteryInfoArray[0]);
					jqsConfirmDTO.setLotteryDateStr(lotteryInfoArray[3]);
					jqsConfirmDTO.setPeilv(lotteryInfoArray[2]);
					jqsConfirmDTO.setGameNumber(lotteryInfoArray[4]);
					String footballMatchId = lotteryInfoArray[1];
					FootballMatch footBallMatch = footballMatchService.findFootballMatchById(Integer.parseInt(footballMatchId));;
					jqsConfirmDTO.setAwayTeam(footBallMatch == null ? "" :footBallMatch.getAwaysxName());
					jqsConfirmDTO.setHomeTeam(footBallMatch == null ? "" :footBallMatch.getHomesxName());
					
					jqsConfirmList.add(jqsConfirmDTO);
				}
				
			}
		}
		
		model.addAttribute("totalMoneyStr", totalMoneyStr);
		model.addAttribute("lotteryCountStr", lotteryCountStr);
		model.addAttribute("lotteryInfoListStr", lotteryInfoListStr);
		model.addAttribute("jqsConfirmList", jqsConfirmList);
		
		return "order/jczqJqsOrderConfirm";
	}
	
	
	/**
	 * 跳转到竞彩足球半全场订单确认页面
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/order/jczq/bqc/orderconfirm")
	public String toJczqbqcOrderConfirm(HttpServletRequest request,Model model){
		String totalMoneyStr=(String)request.getParameter("totalMoney");
		String lotteryCountStr = (String)request.getParameter("lotteryCount");//倍数
		String lotteryInfoListStr = (String)request.getParameter("lotteryInfoList");
		String chuanArrayStr = (String)request.getParameter("chuanArray");

		String[] selectedLotteryInfoArray = null;
		
		List<JqsConfirmDTO> jqsConfirmList = new ArrayList<JqsConfirmDTO>();
		
		if(!StringUtils.isBlank(lotteryInfoListStr)){
			lotteryInfoListStr = lotteryInfoListStr.substring(0, lotteryInfoListStr.length()-1);
			selectedLotteryInfoArray = lotteryInfoListStr.split(";");
			if(selectedLotteryInfoArray != null && selectedLotteryInfoArray.length >0){
				String[] lotteryInfoArray = null;
				for(int i=0;i<selectedLotteryInfoArray.length;i++){
					
					JqsConfirmDTO jqsConfirmDTO =new JqsConfirmDTO();
					lotteryInfoArray = selectedLotteryInfoArray[i].split("_");
					jqsConfirmDTO.setLotteryNo(lotteryInfoArray[0]);
					jqsConfirmDTO.setLotteryDateStr(lotteryInfoArray[3]);
					jqsConfirmDTO.setPeilv(lotteryInfoArray[2]);
					jqsConfirmDTO.setGameNumber(lotteryInfoArray[4]);
					String footballMatchId = lotteryInfoArray[1];
					FootballMatch footBallMatch = footballMatchService.findFootballMatchById(Integer.parseInt(footballMatchId));;
					jqsConfirmDTO.setAwayTeam(footBallMatch == null ? "" :footBallMatch.getAwaysxName());
					jqsConfirmDTO.setHomeTeam(footBallMatch == null ? "" :footBallMatch.getHomesxName());
					
					jqsConfirmList.add(jqsConfirmDTO);
				}
				
			}
		}
		
		String[] selectedChuanArray = null;
		List<JinZuChuanConfirmDTO> chuanConfirmList = new ArrayList<JinZuChuanConfirmDTO>();
		if(!StringUtils.isBlank(chuanArrayStr)){
			chuanArrayStr = chuanArrayStr.substring(0, chuanArrayStr.length()-1);
			selectedChuanArray = chuanArrayStr.split(";");
			for(String chuan:selectedChuanArray){
				JinZuChuanConfirmDTO chuanConfirmDTO = new JinZuChuanConfirmDTO();
				chuanConfirmDTO.setBuyNumber(lotteryCountStr);
				chuanConfirmDTO.setChuanType(chuan);
				chuanConfirmList.add(chuanConfirmDTO);
			}
		}
		
		
		model.addAttribute("totalMoneyStr", totalMoneyStr);
		model.addAttribute("lotteryCountStr", lotteryCountStr);
		model.addAttribute("lotteryInfoListStr", lotteryInfoListStr);
		model.addAttribute("jqsConfirmList", jqsConfirmList);
		model.addAttribute("chuanConfirmList", chuanConfirmList);
		
		return "order/jczqBqcOrderConfirm";
	}
	
	/**
	 * 竞彩足球半全场购买
	 * @return
	 */
	@RequestMapping("/order/jczq_bqc/ajax_save")
	@ResponseBody
	public Map jczqBqcOrderSave(HttpServletRequest request){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("sucess", false);//默认为失败
		
		MemberUser memberUser=(MemberUser)request.getSession().getAttribute(MemberUser.FRONT_MEMBER_LOGIN_SESSION);
		try{
			if(memberUser != null){
				
				String name=(String)request.getParameter("name");
				String totalMoneyStr=(String)request.getParameter("totalMoney");
				String lotteryCountStr = (String)request.getParameter("lotteryCount");
				String lotteryInfoListStr = (String)request.getParameter("lotteryInfoListStr");
				String chuanConfirmListStr = (String)request.getParameter("chuanConfirmListStr");
				
				Integer totalMoney=0;
				Integer lotteryCount = 1;//倍数
				String lotteryType="2串1";
				
				if(!StringUtils.isBlank(chuanConfirmListStr)){
					lotteryType = chuanConfirmListStr;
				}
				
				if(!StringUtils.isBlank(lotteryCountStr)){
					lotteryCount=Integer.parseInt(lotteryCountStr);
				}
				
				if(!StringUtils.isBlank(totalMoneyStr)){
					totalMoney = Integer.parseInt(totalMoneyStr);
				}
				String[] selectedLotteryInfoArray = null;
				
				if(!StringUtils.isBlank(lotteryInfoListStr)){
					lotteryInfoListStr = lotteryInfoListStr.substring(0, lotteryInfoListStr.length()-1);
					selectedLotteryInfoArray = lotteryInfoListStr.split(";");
					
					String orderId = GenerateOrderNoUtil.getOrderNumber();
					
					name = "竞彩足球-半全场-"+totalMoneyStr+"元";
					
					Order order=new Order();
					order.setOrderId(orderId);
					order.setCreateDate(new Date());
					order.setMemberUser(memberUser);
					order.setName(name);
					order.setOrderStatus(Order.ORDER_SUCESS);
					order.setOrderTime(new Date());
					order.setOrderType(Order.PROXY_BUY_ORDER);
					order.setPayStatus(Order.PAY_STATUS_NO);
					order.setTotalMoney(totalMoney);
					order.setUpdateDate(new Date());
					order.setLotteryCount(lotteryCount);
					order.setLotteryType(lotteryType);
					
					orderService.save(order);
					
					for(int i=0;i<selectedLotteryInfoArray.length;i++){
						String[] lotteryArray = selectedLotteryInfoArray[i].split("_");
						OrderDetail orderDetail=new OrderDetail();
						orderDetail.setBuyCaiNumber(lotteryArray[0]);
						orderDetail.setCreateDate(new Date());
						orderDetail.setOrder(order);
						orderDetail.setUpdateDate(new Date());
						
						orderDetailService.save(orderDetail);	
					}
					
					request.getSession().setAttribute("tradeOrder", order);
					
					map.put("sucess", true);
					map.put("msg", "下注成功!");
				}else{
					map.put("sucess", false);
					map.put("msg", "选择参数错误，下注失败!");
				}				
			}else{
				map.put("sucess", false);
				map.put("msg", "未登录!");
			}
		}catch(Exception e){
			e.printStackTrace();
			map.put("sucess", false);
			map.put("msg", "下注失败!");
		}
		return map;
	}
	
	
	/**
	 * 支付成功以后调用
	 */
	public void updateOrderPayStatus(String orderId){
		Order order = orderService.findOrderByOrderId(orderId);
		if(order.getPayStatus() != Order.PAY_STATUS_SUCESS){
			//如果是充值操作则完成积分充值逻辑
			if(order.getOrderType() == Order.RECHARGE_ORDER){
				MemberUser mu = order.getMemberUser();
				mu.setAvailableScore(mu.getAvailableScore()+ order.getTotalMoney());
				mu.setTotalScore(mu.getTotalScore()+ order.getTotalMoney());
				memberUserService.update(mu);
			}
			
			//更改订单支付状态
			order.setPayStatus(Order.PAY_STATUS_SUCESS);
			order.setUpdateDate(new Date());
			orderService.update(order);
		}
	}

}
