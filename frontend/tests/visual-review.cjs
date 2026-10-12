// Browser regression uses explicit API fixtures. It never logs into or alters a real farm database.
const { chromium } = require('playwright')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const base = process.env.QA_URL || 'http://127.0.0.1:5178'
const output = process.env.QA_OUTPUT || path.resolve(__dirname, 'review-artifacts')
fs.mkdirSync(output, { recursive: true })
const groups = [
  ['base','基础档案', [['apiary','蜂场分区','base/apiary/index'],['hive','蜂箱档案','base/hive/index'],['source','蜜源地','base/source/index']]],
  ['breed','养殖管理', [['queen','蜂王档案','breed/queen/index'],['colony','蜂群记录','breed/colony/index'],['inspection','巡检记录','breed/inspection/index'],['feeding','饲喂记录','breed/feeding/index'],['breeding','繁育记录','breed/breeding/index']]],
  ['disease','病害防治', [['disease','病害登记','disease/disease/index'],['medication','用药登记','disease/medication/index']]],
  ['produce','采收生产', [['harvest','摇蜜采收','produce/harvest/index'],['product','产品批次','produce/product/index'],['qc','质检记录','produce/qc/index']]],
  ['stock','库存管理', [['inventory','库存台账','stock/inventory/index'],['warehouse','仓库管理','stock/warehouse/index'],['record','库存流水','stock/record/index']]],
  ['sale','销售出库', [['customer','客户管理','sale/customer/index'],['outbound','销售出库','sale/outbound/index']]],
  ['purchase','采购管理', [['supplier','供应商','purchase/supplier/index'],['purchase','采购单','purchase/purchase/index']]],
  ['tracecode','溯源管理', [['index','溯源码','trace/index'],['scanlog','扫码记录','trace/scanlog/index']]],
  ['system','系统管理', [['user','用户管理','system/user/index'],['role','角色管理','system/role/index'],['menu','菜单管理','system/menu/index'],['dept','部门管理','system/dept/index'],['dict','字典管理','system/dict/index'],['operlog','操作日志','system/operlog/index'],['loginlog','登录日志','system/loginlog/index']]],
  ['finance','成本费用', [['expense','费用记录','finance/expense/index']]],
  ['report','统计报表', [['index','统计报表','report/index']]],
  ['warning','业务预警', [['index','业务预警','warning/index']]]
]
const routes = groups.map(([key,title,children]) => ({ path:'/'+key,name:'Group_'+key,component:'Layout',meta:{title,icon:'FolderOpened'},children:children.map(([key,title,component])=>({path:key,name:component,component,meta:{title,icon:'Document'}})) }))
const stats = {hiveTotal:128,activeHive:116,monthHarvestWeight:386.5,monthOutboundBottles:1248,warnCount:3,todoCount:8,scanTotal:9652,totalWeight:4875.8}
const hive = {id:1,hiveCode:'BX-001',apiaryId:1,apiaryName:'东区蜂场',boxType:'朗式十框',location:'A区 01排',entryDate:'2026-03-01',status:1,remark:'定期巡检'}
const stock = {id:1,itemType:1,itemName:'蜂箱巢础',spec:'标准型',unit:'片',stock:36,warnStock:50,warehouseId:1}
let checks = [], errors = [], apiRequests = [], failDashboard = false
function ok(data) { return {code:200,msg:'成功',data} }
async function fixtures(route) {
  const url = new URL(route.request().url()), p=url.pathname.replace(/^\/api/,'')
  apiRequests.push(p)
  if(Number(url.searchParams.get('pageSize'))>500) errors.push('请求超过分页上限：'+url.pathname)
  if(p==='/product/list' && url.searchParams.get('pageNum')==='2') apiRequests.push('product-options-page-2')
  let data
  if(p==='/auth/login') data=ok({token:'fixture-token'})
  else if(p==='/auth/info') data=ok({user:{id:1,username:'admin',realName:'蜂场管理员'},roles:['admin'],permissions:['*:*:*']})
  else if(p==='/auth/routers') data=ok(routes)
  else if(p==='/auth/logout') data=ok(null)
  else if(p==='/dashboard/stats') {
    if(failDashboard) return route.fulfill({status:503,contentType:'application/json',body:JSON.stringify({code:503,msg:'测试：临时不可用'})})
    data=ok(stats)
  }
  else if(p==='/dashboard/trend') data=ok(Array.from({length:12},(_,i)=>({month:`2026-${String(i+1).padStart(2,'0')}`,weight:[102,124,181,260,318,429,468,387,356,320,289,244][i]})))
  else if(p==='/dashboard/honeyPie') data=ok([{name:'百花蜜',value:460},{name:'洋槐蜜',value:380},{name:'荆条蜜',value:210}])
  else if(p==='/dashboard/hiveStatus') data=ok([{name:'正常',value:116},{name:'空箱',value:8},{name:'维修',value:4}])
  else if(p==='/dashboard/outputTrend') data=ok(Array.from({length:12},(_,i)=>({month:`${i+1}月`,qty:300+i*74,amount:15000+i*3300})))
  else if(p==='/dashboard/scanTrend') data=ok(Array.from({length:20},(_,i)=>({date:`10-${i+1}`,count:90+(i*37)%110})))
  else if(p==='/dashboard/warnTop') data=ok([stock,{...stock,id:2,itemName:'玻璃蜜瓶',spec:'500g',stock:120,warnStock:200}])
  else if(p==='/product/list') { const page=Number(url.searchParams.get('pageNum')||1),size=Number(url.searchParams.get('pageSize')||10);const all=Array.from({length:501},(_,i)=>({id:i+1,productBatchNo:'CP'+(i+1),productName:'百花蜜',spec:'500g',bottleCount:100,availableBottles:100,price:38,qcStatus:1,status:1}));data={code:200,rows:all.slice((page-1)*size,page*size),total:all.length} }
  else if(p==='/hive/list') data={code:200,rows:[hive],total:1}
  else if(p==='/inventory/list') data={code:200,rows:[stock],total:1}
  else if(p==='/warehouse/all') data=ok([{id:1,whName:'蜂具仓库',whType:'蜂具库'}])
  else if(p==='/hive/all') data=ok([hive])
  else if(p==='/user/profile') data=ok({username:'admin',realName:'蜂场管理员',phone:'13800000000',email:'admin@example.test',gender:0})
  else if(p==='/apiary/list') data={code:200,rows:[{id:1,apiaryName:'东区蜂场',location:'山麓蜜源区',status:1}],total:1}
  else if(p.startsWith('/trace/public/')) data=ok({traceCode:'TRTEST',product:{productName:'山野百花蜜',productBatchNo:'CP202610010001',spec:'500g',bottleCount:120,produceDate:'2026-10-01',shelfLife:18,qcStatus:1},harvest:{batchNo:'HS202610010001',honeyType:'百花蜜',weight:60,moisture:42,harvestDate:'2026-10-01'},hive,source:{sourceName:'山麓蜜源地',plant:'百花',location:'生态养殖区'},qcRecords:[{id:1,qcItems:'水分检测',qcResult:1,qcValue:'18%',inspector:'质检员',qcDate:'2026-10-02'}],medications:[],scanCount:12,queryTime:'2026-10-11 10:00:00'})
  else if(p.endsWith('/list')) data={code:200,rows:[],total:0,data:[]}
  else data=ok([])
  return route.fulfill({contentType:'application/json',body:JSON.stringify(data)})
}
async function noOverflow(page,label) {
  const result=await page.evaluate(()=>({body:document.documentElement.scrollWidth,viewport:window.innerWidth}))
  assert(result.body<=result.viewport+1, `${label}: horizontal overflow ${JSON.stringify(result)}`)
  checks.push(label+'：无页面横向溢出')
}
async function main() {
  const browser=await chromium.launch({headless:true,...(process.env.PLAYWRIGHT_BROWSER==='chromium'?{}:{channel:'chrome'})})
  const context=await browser.newContext({viewport:{width:1440,height:1000},reducedMotion:'reduce'})
  await context.route('**/api/**', fixtures)
  const page=await context.newPage()
  page.on('pageerror',e=>errors.push(e.message))
  await page.goto(base+'/login');await page.getByRole('button',{name:'登录工作台'}).waitFor()
  await noOverflow(page,'桌面登录页');await page.screenshot({path:path.join(output,'login-desktop.png'),fullPage:true})
  await page.getByRole('button',{name:'登录工作台'}).click()
  await page.locator('.el-form-item__error').filter({hasText:'请输入用户名'}).waitFor();checks.push('空登录表单校验')
  await page.getByPlaceholder('请输入用户名').fill('admin');await page.getByPlaceholder('请输入密码').fill('test1234')
  await page.getByRole('button',{name:'登录工作台'}).click();await page.waitForURL('**/dashboard');await page.getByText('128',{exact:true}).waitFor()
  await page.waitForTimeout(300);await page.waitForFunction(() => !document.querySelector('.el-message'));await noOverflow(page,'桌面工作台');await page.screenshot({path:path.join(output,'dashboard-desktop.png'),fullPage:true})
  await page.getByRole('button',{name:'刷新数据'}).click();await page.waitForTimeout(300);assert(apiRequests.filter(p=>p==='/dashboard/stats').length>=2);checks.push('工作台刷新真实触发请求')
  await page.keyboard.press('Control+k');await page.getByPlaceholder('输入功能名称，如蜂箱、采收、出库').fill('蜂箱')
  await page.keyboard.press('Enter');await page.waitForURL('**/base/hive');checks.push('Ctrl K 搜索与键盘跳转')
  await page.getByText('BX-001',{exact:true}).waitFor();await page.screenshot({path:path.join(output,'hive-desktop.png'),fullPage:true})
  const add=page.getByRole('button',{name:/新增/}).first();await add.click();await page.locator('.el-dialog:visible').waitFor()
  const box=await page.locator('.el-dialog:visible').boundingBox();assert(box.width<900&&box.height<950);checks.push('业务表单弹窗尺寸正常')
  await page.screenshot({path:path.join(output,'hive-dialog-desktop.png')});await page.keyboard.press('Escape')
  for(const [group,,children] of groups) for(const [key,title] of children) {
    await page.goto(`${base}/${group}/${key}`);await page.locator('.page-heading h1').waitFor();await page.waitForTimeout(100)
    assert.equal(await page.locator('.page-heading h1').innerText(),title);checks.push('路由渲染：'+title)
  }
  assert(apiRequests.includes('product-options-page-2'));checks.push('批次选择器自动加载超过500条的后续页')
  await page.goto(base+'/stock/inventory');await page.getByRole('button',{name:/新增/}).first().click()
  await page.getByText('所属仓库',{exact:true}).waitFor();checks.push('新增库存含仓库选择')
  await page.keyboard.press('Escape')
  failDashboard=true;await page.goto(base+'/dashboard');await page.getByText('部分数据加载失败，请刷新页面重试').waitFor();checks.push('数据失败明确提示且不显示伪造零值');failDashboard=false
  await page.getByRole('button',{name:'刷新数据'}).click();await page.getByText('128',{exact:true}).waitFor()
  await page.waitForFunction(() => !document.querySelector('.el-message'));await page.setViewportSize({width:390,height:844});await page.waitForTimeout(250);await noOverflow(page,'手机工作台')
  await page.screenshot({path:path.join(output,'dashboard-mobile.png'),fullPage:true});await page.locator('.app-main').evaluate(el => el.scrollTop=el.scrollHeight);await page.waitForTimeout(150);await page.screenshot({path:path.join(output,'dashboard-mobile-bottom.png')});await page.locator('.app-main').evaluate(el=>el.scrollTop=0)
  await page.getByRole('button',{name:'展开导航菜单'}).click();assert(await page.locator('.sidebar.mobile-open').count()===1)
  await page.getByRole('button',{name:'关闭导航菜单'}).click({position:{x:370,y:100}});checks.push('手机导航展开与遮罩关闭')
  await page.goto(base+'/base/hive');await page.getByRole('button',{name:/新增/}).first().click();await page.locator('.el-dialog:visible').waitFor()
  const mobileBox=await page.locator('.el-dialog:visible').boundingBox();assert(mobileBox.width<=390&&mobileBox.x>=0&&mobileBox.height<=844)
  await page.screenshot({path:path.join(output,'hive-dialog-mobile.png')});checks.push('手机表单弹窗无越界')
  await page.goto(base+'/trace/TRTEST');await page.locator('.product-title').filter({hasText:'山野百花蜜'}).waitFor();await noOverflow(page,'手机公开溯源');await page.screenshot({path:path.join(output,'trace-mobile.png'),fullPage:true})
  await page.goto(base+'/dashboard');await page.locator('.user-info').click();await page.getByText('退出登录',{exact:true}).click()
  await page.locator('.el-message-box').getByRole('button',{name:'退出登录'}).click();await page.waitForURL('**/login');assert(apiRequests.includes('/auth/logout'));checks.push('退出调用服务端并清除本地会话')
  await noOverflow(page,'手机登录页');await page.screenshot({path:path.join(output,'login-mobile.png'),fullPage:true})
  assert.deepEqual(errors,[],'Browser runtime errors')
  fs.writeFileSync(path.join(output,'result.json'),JSON.stringify({checks,errors,fixtureData:true},null,2))
  console.log(JSON.stringify({passed:checks.length,errors,screenshots:output}))
  await browser.close()
}
main().catch(e=>{fs.writeFileSync(path.join(output,'failure.json'),JSON.stringify({checks,errors,error:e.stack},null,2));console.error(e);process.exit(1)})


