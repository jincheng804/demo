package com.beefarm.sale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beefarm.sale.entity.Customer;
import com.beefarm.sale.mapper.CustomerMapper;
import com.beefarm.sale.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer>
        implements CustomerService {
}
