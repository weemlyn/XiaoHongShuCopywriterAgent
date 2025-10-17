package com.xwm.xiaohongshucopywriteragent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xwm.xiaohongshucopywriteragent.bean.Appointment;
import com.xwm.xiaohongshucopywriteragent.mapper.AppointmentMapper;
import com.xwm.xiaohongshucopywriteragent.service.AppointmentService;
import org.springframework.stereotype.Service;

@Service
public class AppointmentSeviceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {
    @Override
    public Appointment getOne(Appointment appointment) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getUsername,appointment.getUsername());
        queryWrapper.eq(Appointment::getIdCard,appointment.getIdCard());
        queryWrapper.eq(Appointment::getDate,appointment.getDate());
        queryWrapper.eq(Appointment::getDepartment,appointment.getDepartment());
        queryWrapper.orderByDesc(Appointment::getDate)  // 按创建时间倒序
                .last("LIMIT 1");
        return super.getOne(queryWrapper);
    }
}
