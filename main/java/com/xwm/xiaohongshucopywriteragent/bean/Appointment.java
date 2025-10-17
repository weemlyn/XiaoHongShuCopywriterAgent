package com.xwm.xiaohongshucopywriteragent.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Field(name = "患者姓名")
    private String username;

    private String idCard;

    private String department;

    private String date;

    private String time;

    private String doctorName;


    public  String toString(){
        StringBuffer stringBuffer = new StringBuffer();
        String str = stringBuffer.append("患者：").append(username)
                .append(";订单编号：").append(id)
                .append(";身份证号：").append(idCard)
                .append(";挂号科室：").append(department)
                .append("；时间：").append(date).append(time)
                .append(";医生：").append(doctorName)
                .toString();
        return str;
    }
}

