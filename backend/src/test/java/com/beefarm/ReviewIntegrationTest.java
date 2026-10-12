package com.beefarm;

import com.beefarm.common.exception.BusinessException;
import com.beefarm.common.service.BusinessGuard;
import com.beefarm.produce.entity.*;
import com.beefarm.produce.service.*;
import com.beefarm.purchase.entity.*;
import com.beefarm.purchase.service.PurchaseService;
import com.beefarm.sale.entity.*;
import com.beefarm.sale.service.OutboundService;
import com.beefarm.security.*;
import com.beefarm.stock.entity.Inventory;
import com.beefarm.stock.service.InventoryService;
import com.beefarm.system.entity.SysUser;
import com.beefarm.system.service.SysUserService;
import com.beefarm.trace.service.TraceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real Spring services, mapper SQL and transactions against isolated H2 MySQL mode; no user's DB is touched. */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:review;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000;NON_KEYWORDS=VALUE",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:schema-review.sql",
    "bee.jwt.secret=review-test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz-0123456789",
    "logging.level.com.beefarm=ERROR"
})
@AutoConfigureMockMvc
class ReviewIntegrationTest {
    @Autowired JdbcTemplate db;
    @Autowired OutboundService outbound;
    @Autowired ProductBatchService products;
    @Autowired HarvestService harvests;
    @Autowired PurchaseService purchases;
    @Autowired InventoryService inventory;
    @Autowired QcRecordService quality;
    @Autowired TraceService trace;
    @Autowired SysUserService users;
    @Autowired BusinessGuard guard;
    @Autowired JwtTokenProvider tokens;
    @Autowired UserDetailsServiceImpl identities;
    @Autowired PasswordEncoder passwords;
    @Autowired ObjectMapper json;
    @Autowired MockMvc mvc;
    @Autowired PlatformTransactionManager transactions;

    @BeforeEach void prepare() {
        for (String table : List.of("biz_outbound_item","biz_outbound","biz_stock_record","biz_inventory","biz_trace_code","biz_scan_log",
                "biz_qc_log","biz_qc_record","biz_product_batch","biz_harvest","biz_medication","biz_purchase_item","biz_purchase","biz_warehouse",
                "sys_user_role","sys_role_menu","sys_menu","sys_user","sys_role")) db.update("DELETE FROM " + table);
        db.update("INSERT INTO biz_warehouse(id,wh_name,wh_type,status,deleted) VALUES(1,'原料仓','原料库',1,0),(2,'成品仓','成品库',1,0)");
        db.update("INSERT INTO sys_role(id,role_name,role_code,status) VALUES(1,'管理员','admin',1),(2,'销售员','sales',1)");
        String password = passwords.encode("test1234");
        db.update("INSERT INTO sys_user(id,username,password,status,deleted,auth_version) VALUES(1,'admin',?,1,0,0),(2,'sales',?,1,0,0)",password,password);
        db.update("INSERT INTO sys_user_role(user_id,role_id) VALUES(1,1),(2,2)");
        login("admin");
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    void login(String username) {
        var user=identities.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()));
    }
    String token(String username) { return tokens.createToken((LoginUser)identities.loadUserByUsername(username)); }
    void seedProduct(int bottles) {
        db.update("INSERT INTO biz_harvest(id,batch_no,hive_id,harvest_date,weight,honey_type,status,deleted) VALUES(1,'HS202610010001',1,'2026-10-01',10,'百花蜜',2,0)");
        db.update("INSERT INTO biz_product_batch(id,product_batch_no,harvest_id,product_name,spec,bottle_count,qc_status,status,deleted) VALUES(1,'CP202610010001',1,'百花蜜','500g',?,1,1,0)",bottles);
        db.update("INSERT INTO biz_inventory(id,item_type,item_name,spec,unit,stock,warn_stock,warehouse_id,deleted) VALUES(1,2,'百花蜜','500g','瓶',?,1,2,0)",bottles);
        db.update("INSERT INTO biz_stock_record(item_id,warehouse_id,batch_id,biz_type,quantity,stock_after) VALUES(1,2,1,1,?,?)",bottles,bottles);
    }
    Outbound order(int qty) {
        Outbound order=new Outbound();order.setCustomerId(1L);order.setOutboundDate(LocalDate.of(2026,10,1));
        OutboundItem line=new OutboundItem();line.setProductBatchId(1L);line.setQuantity(qty);line.setPrice(new BigDecimal("38.00"));
        order.setItems(List.of(line));return order;
    }
    ProductBatch bottle(Long harvestId,int count) {
        ProductBatch p=new ProductBatch();p.setHarvestId(harvestId);p.setSpec("500g");p.setBottleCount(count);p.setPrice(BigDecimal.TEN);p.setProduceDate(LocalDate.of(2026,10,2));return p;
    }
    void seedHarvest() { db.update("INSERT INTO biz_harvest(id,batch_no,hive_id,harvest_date,weight,honey_type,status,deleted) VALUES(1,'HS202610010001',1,'2026-10-01',10,'百花蜜',1,0)"); }
    Purchase purchase(String qty) {
        Purchase p=new Purchase();p.setWarehouseId(1L);p.setPurchaseDate(LocalDate.of(2026,10,1));
        PurchaseItem line=new PurchaseItem();line.setMaterialName("白砂糖");line.setSpec("袋装");line.setUnit("kg");line.setQuantity(new BigDecimal(qty));line.setPrice(new BigDecimal("6.50"));p.setItems(List.of(line));return p;
    }

    @Test void creatorIsNotClientControlledAndSelfApprovalIsDenied() {
        seedProduct(10);login("sales");var order=order(2);order.setCreateBy("admin");outbound.createOutbound(order);
        assertEquals("sales",outbound.getById(order.getId()).getCreateBy());
        assertThrows(BusinessException.class,()->outbound.audit(order.getId()));
        assertEquals(10,db.queryForObject("SELECT stock FROM biz_inventory WHERE id=1",Integer.class));
    }
    @Test void duplicateConcurrentApprovalDeductsOnce() throws Exception {
        seedProduct(10);login("sales");var order=order(3);outbound.createOutbound(order);
        var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
        Callable<Boolean> approve=()->{ login("admin");start.await();try {outbound.audit(order.getId());return true;}catch(BusinessException e){return false;}finally{SecurityContextHolder.clearContext();}};
        try { var a=pool.submit(approve);var b=pool.submit(approve);start.countDown();assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)); }
        finally {pool.shutdownNow();}
        assertEquals(7,db.queryForObject("SELECT stock FROM biz_inventory WHERE id=1",Integer.class));
        assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM biz_stock_record WHERE biz_type=2",Integer.class));
    }
    @Test void insufficientStockRollsBackWholeOrder() {
        seedProduct(10);login("sales");var order=order(11);outbound.createOutbound(order);login("admin");
        assertThrows(BusinessException.class,()->outbound.audit(order.getId()));
        assertEquals(0,outbound.getById(order.getId()).getStatus());
        assertEquals(10,db.queryForObject("SELECT stock FROM biz_inventory WHERE id=1",Integer.class));
    }
    @Test void batchCannotConsumeStockFromAnotherBatch() {
        seedProduct(2);db.update("UPDATE biz_inventory SET stock=100 WHERE id=1");login("sales");var order=order(3);outbound.createOutbound(order);login("admin");
        assertThrows(BusinessException.class,()->outbound.audit(order.getId()));
    }
    @Test void negativePriceAndUnqualifiedSalesAreRejected() {
        seedProduct(10);var bad=order(1);bad.getItems().get(0).setPrice(new BigDecimal("-1"));assertThrows(BusinessException.class,()->outbound.createOutbound(bad));
        db.update("UPDATE biz_product_batch SET qc_status=0 WHERE id=1");assertThrows(BusinessException.class,()->outbound.createOutbound(order(1)));
    }
    @Test void deletingDraftCleansItemsAndNumbersAreNotReused() {
        seedProduct(10);var first=order(1);String no=outbound.createOutbound(first);outbound.deleteDraft(first.getId());
        assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM biz_outbound_item",Integer.class));
        assertNotEquals(no,outbound.createOutbound(order(1)));
    }
    @Test void auditedOrdersCannotBeDeleted() {
        seedProduct(10);login("sales");var order=order(1);outbound.createOutbound(order);login("admin");outbound.audit(order.getId());
        assertThrows(BusinessException.class,()->outbound.deleteDraft(order.getId()));
    }
    @Test void bottlingCannotRepeatOrExceedHarvestMass() {
        seedHarvest();assertThrows(BusinessException.class,()->products.bottle(bottle(1L,21)));
        products.bottle(bottle(1L,20));assertThrows(BusinessException.class,()->products.bottle(bottle(1L,1)));
        assertEquals(20,db.queryForObject("SELECT stock FROM biz_inventory",Integer.class));
    }
    @Test void deletingUsedBatchDoesNotEraseStock() {
        seedProduct(10);trace.generate(1L);assertThrows(BusinessException.class,()->products.removeBatch(1L));
        assertEquals(10,db.queryForObject("SELECT stock FROM biz_inventory WHERE id=1",Integer.class));
    }
    @Test void cleanBatchDeletionReversesExactlyItsInventory() {
        seedHarvest();var p=bottle(1L,20);products.bottle(p);products.removeBatch(p.getId());
        assertEquals(0,db.queryForObject("SELECT stock FROM biz_inventory",Integer.class));
        assertEquals(1,harvests.getById(1L).getStatus());
        assertEquals(0,db.queryForObject("SELECT SUM(quantity) FROM biz_stock_record",Integer.class));
    }
    @Test void decimalPurchaseQuantityIsPreservedAndRepeatedAuditRejected() {
        var p=purchase("1.75");purchases.createPurchase(p);assertEquals(0,purchases.getById(p.getId()).getTotalQty().compareTo(new BigDecimal("1.75")));
        purchases.audit(p.getId());assertThrows(BusinessException.class,()->purchases.audit(p.getId()));
        assertEquals(0,db.queryForObject("SELECT stock FROM biz_inventory",BigDecimal.class).compareTo(new BigDecimal("1.75")));
    }
    @Test void purchaseEditRejectsNegativeQuantityAndForgedStatus() {
        var p=purchase("2");purchases.createPurchase(p);p.setStatus(1);purchases.updatePurchase(p);assertEquals(0,purchases.getById(p.getId()).getStatus());
        p.getItems().get(0).setQuantity(new BigDecimal("-2"));assertThrows(BusinessException.class,()->purchases.updatePurchase(p));
    }
    @Test void manualStockHasWarehouseAndCannotRewriteProductStock() {
        var item=new Inventory();item.setItemType(1);item.setWarehouseId(1L);item.setItemName("蜂具");item.setSpec("标准");inventory.createItem(item);
        inventory.stockOp(item.getId(),1,new BigDecimal("5"),"入库");
        assertEquals(1L,db.queryForObject("SELECT warehouse_id FROM biz_stock_record",Long.class));
        assertThrows(BusinessException.class,()->inventory.stockOp(item.getId(),2,new BigDecimal("6"),"不足"));
        assertThrows(BusinessException.class,()->inventory.deleteItem(item.getId()));
    }
    @Test void changingQcBatchIsRejectedAndUpdatesKeepHistory() {
        seedProduct(10);QcRecord q=new QcRecord();q.setBatchId(1L);q.setQcResult(1);q.setQcItems("水分");quality.addQc(q);
        q.setBatchId(999L);assertThrows(BusinessException.class,()->quality.updateQcWithLog(q));
        q.setBatchId(1L);q.setQcResult(2);quality.updateQcWithLog(q);assertEquals(2,products.getById(1L).getQcStatus());assertFalse(quality.getQcLogs(q.getId()).isEmpty());
    }
    @Test void previewDoesNotCountAndPublicScanCountsExactlyOnce() {
        seedProduct(10);var code=trace.generate(1L);assertEquals(34,code.getTraceCode().length());
        trace.publicTrace(code.getTraceCode(),null);assertEquals(0,db.queryForObject("SELECT scan_count FROM biz_trace_code",Integer.class));
        var req=new MockHttpServletRequest();req.addHeader("User-Agent","x".repeat(500));req.addHeader("X-Forwarded-For","forged-address");
        trace.publicTrace(code.getTraceCode(),req);assertEquals(1,db.queryForObject("SELECT scan_count FROM biz_trace_code",Integer.class));
        assertEquals(255,db.queryForObject("SELECT LENGTH(user_agent) FROM biz_scan_log",Integer.class));
        assertEquals("127.0.0.1",db.queryForObject("SELECT ip FROM biz_scan_log",String.class));
    }
    @Test void disabledRoleStopsGrantingPermissions() {
        db.update("INSERT INTO sys_menu(id,menu_name,menu_type,perms,status) VALUES(1,'用户','C','user:list',1)");
        db.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(2,1)");assertTrue(((LoginUser)identities.loadUserByUsername("sales")).getPermissions().contains("user:list"));
        db.update("UPDATE sys_role SET status=0 WHERE id=2");assertFalse(((LoginUser)identities.loadUserByUsername("sales")).getPermissions().contains("user:list"));
    }
    @Test void nonAdminCannotAssignAdminRole() {
        login("sales");SysUser user=new SysUser();user.setUsername("intruder");user.setRoleIds(List.of(1L));
        assertThrows(BusinessException.class,()->users.addUser(user));
    }
    @Test void oldTokenRejectedAfterLogout() throws Exception {
        String token=token("admin");SecurityContextHolder.clearContext();
        mvc.perform(post("/auth/logout").header("Authorization","Bearer "+token)).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/auth/info").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test void passwordResetRevokesOldToken() throws Exception {
        String old=token("sales");users.resetPassword(2L,"changed123");SecurityContextHolder.clearContext();
        mvc.perform(get("/auth/info").header("Authorization","Bearer "+old)).andExpect(status().isUnauthorized());
    }
    @Test void unauthorizedDetailAndNegativePagingAreRejected() throws Exception {
        String sales=token("sales");String admin=token("admin");SecurityContextHolder.clearContext();
        mvc.perform(get("/user/1").header("Authorization","Bearer "+sales)).andExpect(jsonPath("$.code").value(403));
        mvc.perform(get("/inventory/list?pageSize=-1").header("Authorization","Bearer "+admin)).andExpect(jsonPath("$.code").value(400));
    }
    @Test void auditFieldsAndPasswordsDoNotLeakThroughJson() throws Exception {
        var user=json.readValue("{\"username\":\"x\",\"password\":\"secret\",\"authVersion\":99,\"createBy\":\"admin\",\"deleted\":1}",SysUser.class);
        assertNull(user.getCreateBy());assertNull(user.getDeleted());assertNull(user.getAuthVersion());
        assertFalse(json.writeValueAsString(user).contains("secret"));
    }
    @Test void guardRequiresATransaction() { assertThrows(org.springframework.transaction.IllegalTransactionStateException.class,()->guard.lock()); }
    @Test void productListShowsBatchBalanceAndPrice() {
        seedProduct(10);db.update("UPDATE biz_product_batch SET price=38.50 WHERE id=1");
        login("sales");var order=order(3);outbound.createOutbound(order);login("admin");outbound.audit(order.getId());
        var product=products.pageList(1,10,new ProductBatch()).getRecords().get(0);
        assertEquals(0,product.getAvailableBottles().compareTo(new BigDecimal("7")));
        assertEquals(0,product.getPrice().compareTo(new BigDecimal("38.50")));
    }
    @Test void manualStockRejectsUnrepresentablePrecision() {
        var item=new Inventory();item.setItemType(1);item.setWarehouseId(1L);item.setItemName("测试蜂具");inventory.createItem(item);
        assertThrows(BusinessException.class,()->inventory.stockOp(item.getId(),1,new BigDecimal("0.001"),"精度"));
        assertEquals(0,inventory.getById(item.getId()).getStock().signum());
    }
    @Test void generatedRoutesHaveUniqueParentAndChildNames() throws Exception {
        db.update("INSERT INTO sys_menu(id,parent_id,menu_name,menu_type,path,component,status,visible) VALUES(1,0,'报表','C','report','report/index',1,1)");
        String token=token("admin");SecurityContextHolder.clearContext();
        mvc.perform(get("/auth/routers").header("Authorization","Bearer "+token))
            .andExpect(jsonPath("$.data[0].name").value("Menu1"))
            .andExpect(jsonPath("$.data[0].children[0].name").value("Page1"));
    }
    @Test void openApiDocumentWorksWithCurrentFramework() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
    @Test void scanDeviceStatsCountAllRecordsAndEnforcePermission() throws Exception {
        for (int i=0;i<1001;i++) db.update("INSERT INTO biz_scan_log(trace_code,device_type,scan_time) VALUES('TEST','Android',CURRENT_TIMESTAMP)");
        db.update("INSERT INTO biz_scan_log(trace_code,device_type,scan_time) VALUES('TEST',NULL,CURRENT_TIMESTAMP)");
        String admin=token("admin"),sales=token("sales");SecurityContextHolder.clearContext();
        mvc.perform(get("/scanlog/deviceStats").header("Authorization","Bearer "+admin))
            .andExpect(jsonPath("$.data[0].name").value("Android")).andExpect(jsonPath("$.data[0].value").value(1001));
        mvc.perform(get("/scanlog/deviceStats").header("Authorization","Bearer "+sales)).andExpect(jsonPath("$.code").value(403));
    }
    @Test void purchaseUsesLineRoundingAndRequiresExistingWarehouse() {
        var p=purchase("1.11");var line=p.getItems().get(0);line.setPrice(new BigDecimal("1.11"));
        var second=new PurchaseItem();second.setMaterialName("白砂糖");second.setQuantity(new BigDecimal("1.11"));second.setPrice(new BigDecimal("1.11"));
        p.setItems(List.of(line,second));purchases.createPurchase(p);
        assertEquals(0,purchases.getById(p.getId()).getTotalAmount().compareTo(new BigDecimal("2.46")));
        p.setWarehouseId(999L);assertThrows(BusinessException.class,()->purchases.updatePurchase(p));
    }
    @Test void legacyPositiveOutgoingQuantitiesStillReduceBatchAvailability() {
        seedProduct(10);db.update("INSERT INTO biz_stock_record(item_id,warehouse_id,batch_id,biz_type,quantity,stock_after) VALUES(1,2,1,2,3,7)");
        assertEquals(0,products.pageList(1,10,new ProductBatch()).getRecords().get(0).getAvailableBottles().compareTo(new BigDecimal("7")));
        login("sales");var order=order(8);outbound.createOutbound(order);login("admin");
        assertThrows(BusinessException.class,()->outbound.audit(order.getId()));
    }
    @Test void downloadedQrUsesCurrentSiteInsteadOfStoredHistoricalUrl() throws Exception {
        seedProduct(10);var code=trace.generate(1L);
        db.update("UPDATE biz_trace_code SET qr_url=? WHERE id=?", "https://example.invalid/old-site/"+code.getTraceCode(),code.getId());
        byte[] png=trace.qrImage(code.getTraceCode());
        var image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
        var bitmap=new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(
                new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image)));
        assertEquals("http://localhost:5173/trace/"+code.getTraceCode(),new com.google.zxing.MultiFormatReader().decode(bitmap).getText());
        assertEquals("http://localhost:5173/trace/"+code.getTraceCode(),trace.pageList(1,10,new com.beefarm.trace.entity.TraceCode()).getRecords().get(0).getQrUrl());
    }
}
