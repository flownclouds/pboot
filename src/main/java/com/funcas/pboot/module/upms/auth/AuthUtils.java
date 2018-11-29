package com.funcas.pboot.module.upms.auth;

import com.funcas.pboot.common.util.CryptoUtils;
import com.funcas.pboot.common.util.EncodeUtils;
import com.funcas.pboot.common.util.FastJsonUtil;
import com.funcas.pboot.conf.SpringContextHolder;
import com.funcas.pboot.module.upms.entity.QrLoginMessage;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author funcas
 * @version 1.0
 * @date 2018年11月28日
 */
public class AuthUtils {

    public static final byte[] DYNAMICKEY = CryptoUtils.generateHmacSha1Key();
    public static final String DYNAMIC_IV = EncodeUtils.encodeBase64(CryptoUtils.generateIV());
    public static final String CHANNEL = "qrLogin";

    public static String generateTicket(long timestamp, String appid){
        String data = DYNAMIC_IV + ";" + timestamp + ";" + appid;
        return EncodeUtils.encodeBase64(CryptoUtils.hmacSha1(DYNAMICKEY, data.getBytes(Charset.forName("UTF-8"))));
    }

    /**
     * 校验签名是否合法
     * 校验二维码是否超时（5分钟）
     * @param sign
     * @param timestamp
     * @param appid
     * @return
     */
    public static boolean verifySign(String sign, long timestamp, String appid){
        boolean signVerify = StringUtils.equals(generateTicket(timestamp, appid), sign);
        if(signVerify){
            return System.currentTimeMillis() - timestamp <= 5 * 60 * 1000;
        }
        return false;
    }

    public static void sendMessage(String ticket, int code, Object ext){
        RedisTemplate redisTemplate = SpringContextHolder.getBean("redisTemplate");
        QrLoginMessage qrLoginMessage = new QrLoginMessage();
        qrLoginMessage.setId(ticket);
        qrLoginMessage.setCode(code);
        qrLoginMessage.setExt(ext);
        qrLoginMessage.setTimestamp(System.currentTimeMillis());
        redisTemplate.convertAndSend(CHANNEL, FastJsonUtil.toJson(qrLoginMessage));
    }

}