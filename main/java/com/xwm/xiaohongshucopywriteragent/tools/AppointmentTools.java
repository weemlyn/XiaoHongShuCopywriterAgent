package com.xwm.xiaohongshucopywriteragent.tools;

import com.xwm.xiaohongshucopywriteragent.bean.Appointment;
import com.xwm.xiaohongshucopywriteragent.service.AppointmentService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentTools {
    @Autowired
    private AppointmentService appointmentService;

    @Tool(name = "预约咨询",value = "根据参数，先执行工具方法queryDepartment查询是否可预约，并直 接给用户回答是否可预约，" +
            "并让用户确认所有预约信息，用户确认后再进行预约。" +
            "如果用户没有提供具体的心理咨询师姓名，请从向量存储中找到一位心理咨询师。")
    public String bookAppointment(Appointment appointment){
        Appointment appointmentdb = appointmentService.getOne(appointment);
        if (appointmentdb == null){
            appointment.setId(null);
            if (appointmentService.save(appointment)){
                return "预约成功，并返回预约详情";
            }
            return "预约失败";
        }
        return "您想预约的咨询师在相同的时间已有预约";
    }


}
