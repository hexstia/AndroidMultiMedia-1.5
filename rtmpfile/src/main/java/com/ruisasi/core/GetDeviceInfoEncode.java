package com.ruisasi.core;

import com.alibaba.fastjson.JSONObject;
import com.wangheart.rtmpfile.rtmp.encode.ApcMsgBodyEncode;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

public class GetDeviceInfoEncode implements ApcMsgBodyEncode {

    @Override
    //服务器设置设备信息   解析服务器传过来的设备信息
    public JSONObject praseMsgBody(byte[] bodyBytes) {
        JSONObject body = new JSONObject();
        int index = 0;
        //mac
        String mac = ApcMsg.byte2str(bodyBytes, index, 17);
        index += 17;

        int len = bodyBytes[index]&0xff;
        index++;
        String model = ApcMsg.byte2str(bodyBytes, index, len);
        index += len;

        len = bodyBytes[index]&0xff;
        index++;
        String brand = ApcMsg.byte2str(bodyBytes, index, len);
        index += len;

        len = bodyBytes[index]&0xff;
        index++;
        String device = ApcMsg.byte2str(bodyBytes, index, len);
        index += len;

        len = bodyBytes[index]&0xff;
        index++;
        String board = ApcMsg.byte2str(bodyBytes, index, len);
        index += len;

        len = bodyBytes[index]&0xff;
        index++;
        String manufacturer = ApcMsg.byte2str(bodyBytes, index, len);
//      index += len;
        body.put("mac", mac);
        body.put("model", model);
        body.put("brand", brand);
        body.put("device", device);
        body.put("board", board);
        body.put("manufacturer", manufacturer);

        return body;
    }

    @Override
    public byte[] getBytes(JSONObject body) {
        byte[] res = null;
        try {
            res = new byte[1];
             res[0] = body.getByte("code");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}
