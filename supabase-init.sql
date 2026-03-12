-- 智能年鉴编纂系统 - Supabase 初始化SQL
-- 请在 Supabase Dashboard > SQL Editor 中执行此脚本

-- 用户表
CREATE TABLE IF NOT EXISTS yb_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'editor',
    avatar_color VARCHAR(20) DEFAULT '#1a90ff',
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 年鉴表
CREATE TABLE IF NOT EXISTS yb_yearbooks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    cover_type VARCHAR(20) DEFAULT 'default_1',
    cover_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',
    progress INT NOT NULL DEFAULT 0,
    created_by UUID REFERENCES yb_users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 年鉴负责人关联表
CREATE TABLE IF NOT EXISTS yb_yearbook_managers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    yearbook_id UUID NOT NULL REFERENCES yb_yearbooks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES yb_users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(yearbook_id, user_id)
);

-- 大纲节点表
CREATE TABLE IF NOT EXISTS yb_outlines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    yearbook_id UUID NOT NULL REFERENCES yb_yearbooks(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES yb_outlines(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',
    unit_name VARCHAR(100),
    assigned_user_id UUID REFERENCES yb_users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 条目表
CREATE TABLE IF NOT EXISTS yb_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    outline_id UUID NOT NULL REFERENCES yb_outlines(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    original_content TEXT,
    ai_content TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    created_by UUID REFERENCES yb_users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 条目版本表
CREATE TABLE IF NOT EXISTS yb_entry_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id UUID NOT NULL REFERENCES yb_entries(id) ON DELETE CASCADE,
    version INT NOT NULL,
    content TEXT NOT NULL,
    revision_note VARCHAR(500),
    editor_id UUID REFERENCES yb_users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 批注表
CREATE TABLE IF NOT EXISTS yb_annotations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id UUID NOT NULL REFERENCES yb_entries(id) ON DELETE CASCADE,
    content VARCHAR(200) NOT NULL,
    author_id UUID REFERENCES yb_users(id),
    annotation_type VARCHAR(20) DEFAULT 'comment',
    process_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 资料文件夹表（供稿单位）
CREATE TABLE IF NOT EXISTS yb_material_folders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_name VARCHAR(100) NOT NULL UNIQUE,
    tags TEXT,
    file_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 资料文件表
CREATE TABLE IF NOT EXISTS yb_material_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_id UUID NOT NULL REFERENCES yb_material_folders(id) ON DELETE CASCADE,
    outline_id UUID REFERENCES yb_outlines(id),
    file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(20),
    upload_year INT,
    source VARCHAR(30),
    uploaded_by UUID REFERENCES yb_users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 历史数据表
CREATE TABLE IF NOT EXISTS yb_history_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    yearbook_name VARCHAR(100),
    outline_section VARCHAR(200),
    entry_title VARCHAR(200),
    content TEXT,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引提升查询性能
CREATE INDEX IF NOT EXISTS idx_outlines_yearbook ON yb_outlines(yearbook_id);
CREATE INDEX IF NOT EXISTS idx_outlines_parent ON yb_outlines(parent_id);
CREATE INDEX IF NOT EXISTS idx_entries_outline ON yb_entries(outline_id);
CREATE INDEX IF NOT EXISTS idx_entry_versions_entry ON yb_entry_versions(entry_id);
CREATE INDEX IF NOT EXISTS idx_annotations_entry ON yb_annotations(entry_id);
CREATE INDEX IF NOT EXISTS idx_material_files_folder ON yb_material_files(folder_id);
CREATE INDEX IF NOT EXISTS idx_yearbook_managers_yearbook ON yb_yearbook_managers(yearbook_id);

-- 插入测试数据（可选）
-- 注意：密码均为 BCrypt 加密后的值
-- admin/admin123, editor1/123456, editor2/123456
-- 系统启动时会自动通过 DataInitializer 创建这些用户
-- 如需手动插入，请先获取 BCrypt 密文

-- 插入历史参考数据
INSERT INTO yb_history_data (yearbook_name, outline_section, entry_title, content, year)
VALUES 
  ('江苏年鉴2023', '政治建设', '人大工作', '2023年，市人民代表大会常务委员会深入贯彻落实党的二十大精神，全年召开常委会会议12次，审议通过地方性法规3件，听取专项工作报告14个，开展执法检查5次，专题询问2次。组织人大代表视察活动8次，收集代表意见建议142条。', 2023),
  ('南京年鉴2023', '经济发展', 'GDP总量', '2023年，全市实现地区生产总值（GDP）17,400亿元，同比增长6.8%，增速位居全省前列。其中，第一产业增加值190亿元，第二产业增加值6,200亿元，第三产业增加值11,010亿元，三次产业结构调整为1.1:35.6:63.3。', 2023),
  ('苏州年鉴2022', '社会事业', '教育发展', '2022年，全市教育事业持续健康发展。年内新建、改扩建中小学校32所，新增学位2.8万个。全市学前教育入园率达99.2%，义务教育巩固率达99.8%，高中阶段教育普及率达99.5%。', 2022),
  ('江苏年鉴2022', '科技创新', '科技投入', '2022年，全省研究与试验发展（R&D）经费支出占地区生产总值比重达3.05%，居全国第三。新增高新技术企业3621家，全省高新技术企业总数超4万家。新增省级以上企业技术中心85家、工程技术研究中心126家。', 2022)
ON CONFLICT DO NOTHING;

RAISE NOTICE '数据库初始化完成！';
