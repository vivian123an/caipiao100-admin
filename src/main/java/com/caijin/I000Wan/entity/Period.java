package com.caijin.I000Wan.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 彩票期数实体
 * @author jeff
 * @since 2014-06-20 22:00
 */
@Entity
@Table(name = "lottery_period")
public class Period{

	private static final long serialVersionUID = 7196198838990921150L;
	
	public final static Integer STATUS_CURRENT=1;//当前期
	
	public final static Integer STATUS_BEFORE=0;//以往期
	
	public final static Integer STATUS_AFTER=2;//预售期
	
	public final static String FOOTBALL_SFC="zc_sfc";//足彩胜负彩
	
	public final static String FOOTBALL_SIXCB="zc_zc6";//足彩六场半全场
	
	public final static String FOOTBALL_FOURJQ="zc_jq4";//足彩四场进球
	
	public final static String FOOTBALL_RX9="zc_rx9";//任选九场
	
	private Integer id;

	private String lotteryCode;//彩票编码
	
	private String lotteryPeriod;//彩票期数
	
	private Integer status;//期数状态 如当前期、以往期、预售期
	
	/**
	 * 创建日期
	 */
	private Date createDate;
	/**
	 * 修改日期
	 */
	private Date updateDate;
	
	private Date endTime;//当期彩票截止时间
	
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", nullable = true)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name="lottery_code",length=50)
	public String getLotteryCode() {
		return lotteryCode;
	}

	public void setLotteryCode(String lotteryCode) {
		this.lotteryCode = lotteryCode;
	}

	@Column(name="lottery_period",length=100)
	public String getLotteryPeriod() {
		return lotteryPeriod;
	}

	public void setLotteryPeriod(String lotteryPeriod) {
		this.lotteryPeriod = lotteryPeriod;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name="create_date")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Column(name="update_date")
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="period_end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	
	
}
