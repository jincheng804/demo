import { ElMessageBox } from 'element-plus'
// 用户取消是正常交互；其他异常仍交给调用方处理。
export async function confirmAction(...args) {
  try {
    await ElMessageBox.confirm(...args)
    return true
  } catch (error) {
    if (error === 'cancel' || error === 'close') return false
    throw error
  }
}
export async function validateForm(form) {
  if (!form) return false
  // 校验失败仍在原字段展示提示，避免未处理的 Promise。
  return form.validate().then(
    () => true,
    () => false
  )
}
