"""
地方志智能编修平台 - AI 服务
基于 FastAPI + LangChain 的 RAG 架构
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import date
import os

app = FastAPI(title="地方志 AI 服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============ 请求/响应模型 ============

class SummarizeRequest(BaseModel):
    content: str
    length: int = 500


class GenerateEntryRequest(BaseModel):
    title: str
    context: str = ""


class SearchRequest(BaseModel):
    query: str
    year: Optional[int] = None
    department: Optional[str] = None
    limit: int = 10


class AskRequest(BaseModel):
    question: str


# ============ AI 模拟实现（可替换为真实 LLM） ============

def _mock_summarize(content: str, length: int) -> str:
    """模拟摘要生成"""
    if len(content) <= length:
        return content
    return content[:length] + "...（摘要）"


def _mock_generate_entry(title: str, context: str) -> str:
    """模拟条目生成"""
    return f"""【{title}】

（此处为 AI 生成的志书条目初稿，实际部署时可接入大模型如 GPT、文心一言等）

一、概述
根据相关资料整理，{title}相关内容如下。

二、主要内容
{context[:200] if context else "（待补充）"}...

三、数据与成效
（可结合统计数据自动生成）

四、发展趋势
（可结合历史趋势分析生成）
"""


def _mock_search(query: str, year: Optional[int], department: Optional[str], limit: int) -> List[Dict]:
    """模拟知识库检索"""
    return [
        {
            "title": f"检索结果：{query}",
            "content": "相关文档内容片段...",
            "source": "知识库",
            "score": 0.95,
        }
    ][:limit]


def _mock_ask(question: str) -> str:
    """模拟问答"""
    return f"根据知识库检索，关于「{question}」：\n\n（实际部署时接入 RAG + 大模型生成准确答案）"


def _mock_generate_events() -> List[Dict]:
    """模拟大事记生成"""
    return [
        {"title": "XX高速公路开工", "content": "2024年3月，XX高速公路正式开工建设。", "time": "2024-03-15"},
        {"title": "XX工业园投产", "content": "2024年5月，XX工业园正式投产。", "time": "2024-05-20"},
    ]


# ============ API 路由 ============

@app.post("/api/summarize")
def summarize(req: SummarizeRequest):
    return {"summary": _mock_summarize(req.content, req.length)}


@app.post("/api/generate-entry")
def generate_entry(req: GenerateEntryRequest):
    return {"content": _mock_generate_entry(req.title, req.context)}


@app.post("/api/search")
def search(req: SearchRequest):
    return _mock_search(req.query, req.year, req.department, req.limit)


@app.post("/api/ask")
def ask(req: AskRequest):
    return {"answer": _mock_ask(req.question)}


@app.post("/api/generate-events")
def generate_events():
    return _mock_generate_events()


@app.get("/health")
def health():
    return {"status": "ok"}
