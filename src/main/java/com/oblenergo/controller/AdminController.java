package com.oblenergo.controller;

import com.oblenergo.DTO.OrderDTO;
import com.oblenergo.DTO.TimeDTO;
import com.oblenergo.DTO.WorkTypeDTO;
import com.oblenergo.editor.CarEditor;
import com.oblenergo.editor.ServiceEditor;
import com.oblenergo.enums.StatusOrderEnum;
import com.oblenergo.model.Car;
import com.oblenergo.model.Orders;
import com.oblenergo.model.WorkType;
import com.oblenergo.service.*;
import com.oblenergo.validator.WorkTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

  private static final String ITEMSWORKTYPE = "typeWorks";
  private static final String ITEMSORDER = "order";
  private static final String ITEMSCAR = "cars";
  private static final String WORK_TYPE = "workType";
  private static final String ORDER = "orders";
  private static final String STATUS_ORDER_ENUM = "items";
  private static final String WORKTYPE_FROM_SAP = "workTypeFromSap";
  
  private static final String ORDER_CONFIRMED = "\u0412\u0430\u0448\u0435 \u0437\u0430\u043C\u043E\u0432\u043B\u0435\u043D\u043D\u044F \u043F\u0456\u0434\u0442\u0432\u0435\u0440\u0434\u0436\u0435\u043D\u0435";
  private static final String ORDER_CENCELED = "\u0412\u0430\u0448\u0435 \u0437\u0430\u043C\u043E\u0432\u043B\u0435\u043D\u043D\u044F \u0441\u043A\u0430\u0441\u043E\u0432\u0430\u043D\u0435";

  @Autowired
  private MailService mailServiceImpl;

  @Autowired
  private OrderService orderServiceImpl;

  @Autowired
  private CarService carServiceImpl;

  @Autowired
  private WorkTypeService workTypeServiceImpl;

  @Autowired
  private ServiceEditor serviceEditor;

  @Autowired
  private CarEditor carEditor;

  @Autowired
  private SapService sapServiceImpl;

  @Autowired
  private WorkTypeValidator workTypeValidator;

  @InitBinder("workType")
  public void initBinder(WebDataBinder binder) {
    binder.setValidator(workTypeValidator);
  }

  @InitBinder(ORDER)
  public void initBinderOrder(WebDataBinder binder) {
    binder.registerCustomEditor(WorkType.class, serviceEditor);
    binder.registerCustomEditor(Car.class, carEditor);
  }

  @RequestMapping(method = RequestMethod.GET)
  public String getAllType(Model model) {

    model.addAttribute(ITEMSWORKTYPE, workTypeServiceImpl.findAll());
    model.addAttribute(WORK_TYPE, new WorkType());
    return "workType";
  }

  @RequestMapping(value = "/workType/newWorkType", method = RequestMethod.GET)
  public String redirectToCreate(Model model) {

    model.addAttribute(WORK_TYPE, new WorkType());
    return "updateCreateWorkType";
  }

  @RequestMapping(value = "/workType/newWorkType", method = RequestMethod.POST)
  public String addType(@Validated @ModelAttribute("workType") WorkType workType, BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute(WORK_TYPE, workType);
      return "updateCreateWorkType";
    }
    WorkTypeDTO wkDTO = workTypeServiceImpl.getWorkTypeDTOByIdFromSAP(workType.getId());
    workType.setName(wkDTO.getName());
    workType.setEnabled(false);
    workTypeServiceImpl.save(workType);
    return "redirect:/admin";
  }

  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWorkType(@RequestBody String id) {
    workTypeServiceImpl.delete(id);
  }

  @RequestMapping(value = "/order", method = RequestMethod.GET)
  public String getAllOrders(Model model) {
    model.addAttribute(ITEMSORDER, orderServiceImpl.findAll());
    model.addAttribute(ORDER, new Orders());
    return "order";
  }

  @RequestMapping(value = "/order/{id}", method = RequestMethod.GET)
  public String showTypeOrderById(@PathVariable int id, Model model) {
    model.addAttribute(ORDER, orderServiceImpl.findOrderById(id));
    model.addAttribute(ITEMSWORKTYPE, workTypeServiceImpl.findAll());
    model.addAttribute(ITEMSCAR, carServiceImpl.findAll());
    model.addAttribute(STATUS_ORDER_ENUM, StatusOrderEnum.values());
    model.addAttribute(WORKTYPE_FROM_SAP, sapServiceImpl.getAllWorkTypes());
    return "updateCreateOrders";
  }

  @RequestMapping(value = "/order/{id}", method = RequestMethod.POST)
  public String updateOrder(@Validated @ModelAttribute("orders") Orders orders, BindingResult bindingResult,
      Model model) {

    if (bindingResult.hasErrors()) {
      model.addAttribute(ORDER, orders);
      model.addAttribute(ITEMSWORKTYPE, workTypeServiceImpl.findAll());
      model.addAttribute(ITEMSCAR, carServiceImpl.findAll());
      model.addAttribute(STATUS_ORDER_ENUM, StatusOrderEnum.values());
      model.addAttribute(WORKTYPE_FROM_SAP, sapServiceImpl.getAllWorkTypes());
      return "updateCreateOrders";
    }

    // this piece of code should moved to some service
    if (orders.getStatus_order().equals(StatusOrderEnum.DONE)
        && (!orderServiceImpl.findOrderById(orders.getId()).getStatus_order().equals(orders.getStatus_order()))) {
      OrderDTO orderDTO = sapServiceImpl.createNewOrder(orders.getCar_number(), orders.getWorkType().getId(),
          Integer.toString(orders.getCount()), orders.getUser_tab());
      orders.setBill_number(orderDTO.getOrderNum());
      if (orders.getSecond_email().equals("")) {
        mailServiceImpl.sendMail(orderDTO, orders, sapServiceImpl.getUserEmailFromSap(orders.getUser_tab()),
            ORDER_CONFIRMED);
      } else {
        mailServiceImpl.sendMail(orderDTO, orders, orders.getSecond_email(), ORDER_CONFIRMED);
      }

    } else if (orders.getStatus_order().equals(StatusOrderEnum.DONE)
        && (orderServiceImpl.findOrderById(orders.getId()).getStatus_order().equals(orders.getStatus_order()))) {
      if (orders.getSecond_email().equals("")) {
        mailServiceImpl.sendMailOnlyPermit(orders, sapServiceImpl.getUserEmailFromSap(orders.getUser_tab()),
            ORDER_CONFIRMED);
      } else
        mailServiceImpl.sendMailOnlyPermit(orders, orders.getSecond_email(), ORDER_CONFIRMED);
    } else if (orders.getStatus_order().equals(StatusOrderEnum.CANCELED)) {
      if (orders.getSecond_email().equals("")) {
        mailServiceImpl.sendMailWithoutPDF(sapServiceImpl.getUserEmailFromSap(orders.getUser_tab()), ORDER_CENCELED);
      } else
        mailServiceImpl.sendMailWithoutPDF(orders.getSecond_email(), ORDER_CENCELED);
    }
    orderServiceImpl.update(orders);
    return "redirect:/admin/order";

  }

  @RequestMapping(value = "/order/delete", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteType(@RequestBody int id) {
    orderServiceImpl.delete(id);
  }

  @RequestMapping(value = "/changeStatusWorkType", headers = "Accept=*/*", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
  public @ResponseBody void selectTimeForDateAdmin(@RequestBody WorkTypeDTO workTypeDTO) {
    String idWorkType = workTypeDTO.getId();
    workTypeServiceImpl.update(idWorkType);
  }

  @RequestMapping(value = "/selectTimeAdmin", headers = "Accept=*/*", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
  public @ResponseBody String[] selectTimeForDateAdmin(@RequestBody TimeDTO timeDTO) {

    Orders order = orderServiceImpl.findOrderById(Integer.parseInt(timeDTO.getId()));
    List<Orders> orders = orderServiceImpl.findDateOfOrders(timeDTO.getDate());
    String[][] arrTimeOrders = orderServiceImpl.getAllTimeOfOrders(orders);
    List<String> freeTime = orderServiceImpl.findFreeTimeForAdmin(arrTimeOrders, timeDTO.getDate(), order,
        timeDTO.getTimeExecution());
    String[] arr = freeTime.toArray(new String[freeTime.size()]);
    return arr;
  }

}
