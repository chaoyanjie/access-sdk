package cn.bubi.access.adaptation.blockchain.bc.request;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 签名信息
 * @author hobo
 *
 */
public class SignatureRequest {
	@JSONField(name = "public_key")
	private String publicKey;
	@JSONField(name = "sign_data")
	private String signData;
	
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getSignData() {
		return signData;
	}
	public void setSignData(String signData) {
		this.signData = signData;
	}

}
