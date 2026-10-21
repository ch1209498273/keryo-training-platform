/**
 * 富文本安全清洗工具（防存储型 XSS）
 *
 * 题库 content / 选项 / 解析 均为 HTML 富文本，经管理端出题或 Excel 导入原样入库，
 * 学员端用 v-html 渲染。渲染前必须经本函数清洗，去除 script / on* 事件 / javascript: 协议等。
 */
import DOMPurify from 'dompurify';

// 教学排版所需的白名单标签
const ALLOWED_TAGS = [
  'p', 'br', 'b', 'strong', 'i', 'em', 'u', 's',
  'ul', 'ol', 'li', 'span', 'div', 'blockquote',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'img', 'a', 'table', 'thead', 'tbody', 'tr', 'td', 'th',
  'code', 'pre', 'sub', 'sup', 'hr', 'font',
];

const ALLOWED_ATTR = ['src', 'href', 'title', 'alt', 'target', 'rel', 'width', 'height', 'style'];

export function sanitizeHtml(html: string | null | undefined): string {
  if (!html) return '';
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS,
    ALLOWED_ATTR,
    ALLOW_DATA_ATTR: false,
    FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed', 'form', 'input'],
    FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus', 'onblur', 'onsubmit', 'onchange', 'onkeydown', 'onkeyup', 'style-src'],
  });
}

