package com.xwm.xiaohongshucopywriteragent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xwm.xiaohongshucopywriteragent.bean.Appointment;


public interface AppointmentService extends IService<Appointment> {

    Appointment getOne(Appointment appointment);

}
