package com.caijin.I000Wan.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.caijin.I000Wan.common.entity.BaseEntity;

/**
 * 订单表
 * 
 * @author Jeff
 * @since 2014-07-21
 */

@Entity
@Table(name = "trade_order")
public class Order extends BaseEntity {

	private static final long serialVersionUID = 7485168642747519619L;

	public final static int RECHARGE_ORDER = 1;

	public final static int PROXY_BUY_ORDER = 2;

	public final static int ORDER_SUCESS = 1;

	public final static int ORDER_FAILUE = 2;
	
	public final static int PAY_STATUS_NO=0;//未支付
	
	public final static int PAY_STATUS_SUCESS=1;//支付成功
	
	public final static int PAY_STATUS_FAILUE=2;//支付失败
	
	public final static int PAY_STATUS_OFFTIME=3;//支付超时

	private String name;
	
	private String orderNo;//orderNo 

	private Integer orderType;// 订单类型 1为订单充值 2为代购订单

	private Integer orderStatus;// 订单状态类型 1为下订单成功 2为下订单失败

	private Integer payStatus;// 支付状态类型 0为未支付 1为支付成功 2为支付失败 3为支付超时
	
	private String orderId; //订单ID

	private Date orderTime;// 下订单时间

	private MemberUser memberUser;

	private Period period;// 彩票期数

	private Integer totalMoney;// 总金额
	
	private Integer lotteryCount; //彩票注数
	
	private String lotteryType;//彩票类型

	@Column(length = 200)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    @Column(name="order_no",length=32)
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	@Column(precision = 1, name = "order_type")
	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}

	@Column(precision = 1, name = "order_status")
	public Integer getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Column(precision = 1, name = "pay_status")
	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}

	@Column(name="order_id",length=36)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "order_time")
	@Temporal(TemporalType.DATE)
	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	@ManyToOne
	@JoinColumn(name = "member_id")
	public MemberUser getMemberUser() {
		return memberUser;
	}

	public void setMemberUser(MemberUser memberUser) {
		this.memberUser = memberUser;
	}

	@ManyToOne
	@JoinColumn(name = "lottery_period")
	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	@Column(precision = 10, name = "total_money")
	public Integer getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(Integer totalMoney) {
		this.totalMoney = totalMoney;
	}

	@Column(name="lottery_count")
	public Integer getLotteryCount() {
		return lotteryCount;
	}

	public void setLotteryCount(Integer lotteryCount) {
		this.lotteryCount = lotteryCount;
	}

	@Column(name="lottery_type")
	public String getLotteryType() {
		return lotteryType;
	}

	public void setLotteryType(String lotteryType) {
		this.lotteryType = lotteryType;
	}
	
	

}
