package com.tencent.qcloud.suixinbo.model;

/**
 * 消息体类
 */
public class ChatEntity {
	
	private String grpSendName;
	private String context;
	private int  type;

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	//wzw add 置顶消息才有，取消置顶根据该属性判断
	private String sequence;


	public ChatEntity() {
		// TODO Auto-generated constructor stub
	}
	


	public String getSenderName() {
		return grpSendName;
	}

	public void setSenderName(String grpSendName) {
		this.grpSendName = grpSendName;
	}
		


	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
