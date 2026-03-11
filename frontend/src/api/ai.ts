/**
 * AI Mock 服务 —— 当前阶段全部返回模拟数据
 */

function delay(ms: number = 1500) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export async function aiGenerateEntry(rawData: string, historyData?: string): Promise<string> {
  await delay(2000)
  return `<p>【AI生成条目】</p>
<p>根据提供的原始数据分析，${new Date().getFullYear()}年度相关工作取得显著进展。全年共完成重点项目12个，同比增长15.3%。其中，基础设施建设投资达到128.5亿元，较上年增加23.6亿元。</p>
<p>在政策推进方面，先后出台配套文件8项，覆盖经济发展、社会治理、民生保障等多个领域。全年地区生产总值实现稳步增长，城镇居民人均可支配收入达到52800元，增长6.2%。</p>
<p>社会事业持续改善，新增就业岗位3.5万个，城镇登记失业率控制在3.1%以内。教育、医疗、文化等公共服务水平进一步提升。</p>`
}

export async function aiRewrite(text: string): Promise<string> {
  await delay(1500)
  return `<p>【AI润色结果】</p><p>${text.replace(/<[^>]*>/g, '').substring(0, 100)}……经AI润色优化后，文本表述更加规范、精炼，数据引用更加准确，符合年鉴编纂规范要求。全文逻辑结构清晰，语言简洁凝练，重点突出。</p>`
}

export async function aiExpand(text: string): Promise<string> {
  await delay(1500)
  const plain = text.replace(/<[^>]*>/g, '').substring(0, 80)
  return `<p>【AI扩写结果】</p>
<p>${plain}</p>
<p>在此基础上，进一步深化改革措施，推动高质量发展取得新突破。全年经济运行总体平稳，主要指标保持在合理区间。产业结构持续优化，第三产业增加值占比提升至54.3%。创新驱动发展战略深入实施，全社会研发经费投入强度达到2.85%。</p>
<p>区域协调发展格局加快形成，城乡融合发展体制机制逐步完善。生态环境质量持续好转，PM2.5年均浓度同比下降8.6%，优良天数比例达到82.1%。</p>`
}

export async function aiDetectConflicts(content: string): Promise<Array<{
  type: string
  description: string
  location: string
  severity: 'high' | 'medium' | 'low'
}>> {
  await delay(1000)
  return [
    { type: '数据冲突', description: 'GDP数据与统计局公报数据不一致（偏差2.3%）', location: '第2段第3行', severity: 'high' },
    { type: '时间错误', description: '事件发生时间与上下文不符', location: '第3段第1行', severity: 'medium' },
    { type: '格式规范', description: '百分比数据应保留一位小数', location: '第1段第2行', severity: 'low' },
  ]
}

export async function aiSearchHistory(keyword: string): Promise<Array<{
  year: number
  title: string
  content: string
}>> {
  await delay(1000)
  return [
    { year: 2023, title: `${keyword} - 2023年度`, content: `2023年，${keyword}相关工作稳步推进，全年完成投资额98.2亿元，同比增长12.5%。重点推进了3个省级示范项目建设。` },
    { year: 2022, title: `${keyword} - 2022年度`, content: `2022年，面对复杂严峻的外部环境，${keyword}工作克服困难，全年完成投资额87.3亿元。积极落实各项惠企政策。` },
    { year: 2021, title: `${keyword} - 2021年度`, content: `2021年，${keyword}领域取得重大突破，全年实现营业收入215亿元，利税总额达到31.6亿元。` },
  ]
}

export async function aiBotAnswer(question: string): Promise<string> {
  await delay(1200)
  return `关于"${question}"的回答：\n\n根据年鉴编纂规范和相关数据分析，该问题涉及多个方面。建议参考最新统计年报数据，结合实际工作情况进行核实。年鉴条目撰写应注意数据准确性、时间节点一致性和表述规范性。如需进一步了解，可查阅相关政策文件或咨询对应业务部门。`
}
