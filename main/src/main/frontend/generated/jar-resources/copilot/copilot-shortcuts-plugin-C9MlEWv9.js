import { u as f, b as c, E as g, F as i, a1 as b, X as s, H as m } from "./copilot-CH8EJMw_.js";
import { B as $ } from "./base-panel-B2een6M0.js";
import { i as e } from "./icons-Ca9sB-3j.js";
const v = 'copilot-shortcuts-panel{display:flex;flex-direction:column;padding:var(--space-150)}copilot-shortcuts-panel h3{font:var(--font-xsmall-semibold);margin-bottom:var(--space-100);margin-top:0}copilot-shortcuts-panel h3:not(:first-of-type){margin-top:var(--space-200)}copilot-shortcuts-panel ul{display:flex;flex-direction:column;list-style:none;margin:0;padding:0}copilot-shortcuts-panel ul li{display:flex;align-items:center;gap:var(--space-50);position:relative}copilot-shortcuts-panel ul li:not(:last-of-type):before{border-bottom:1px dashed var(--border-color);content:"";inset:auto 0 0 calc(var(--size-m) + var(--space-50));position:absolute}copilot-shortcuts-panel ul li span:has(svg){align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}copilot-shortcuts-panel .kbds{margin-inline-start:auto}copilot-shortcuts-panel kbd{align-items:center;border:1px solid var(--border-color);border-radius:var(--radius-2);box-sizing:border-box;display:inline-flex;font-family:var(--font-family);font-size:var(--font-size-1);line-height:var(--line-height-1);padding:0 var(--space-50)}', u = window.Vaadin.copilot.tree;
if (!u)
  throw new Error("Tried to access copilot tree before it was initialized.");
var y = Object.getOwnPropertyDescriptor, w = (t, l, h, p) => {
  for (var o = p > 1 ? void 0 : p ? y(l, h) : l, n = t.length - 1, r; n >= 0; n--)
    (r = t[n]) && (o = r(o) || o);
  return o;
};
let d = class extends $ {
  constructor() {
    super(), this.onTreeUpdated = () => {
      this.requestUpdate();
    };
  }
  connectedCallback() {
    super.connectedCallback(), c.on("copilot-tree-created", this.onTreeUpdated);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), c.off("copilot-tree-created", this.onTreeUpdated);
  }
  render() {
    const t = u.hasFlowComponents();
    return i`<style>
        ${v}
      </style>
      <h3>Global</h3>
      <ul>
        <li>
          <span>${e.vaadinLogo}</span>
          <span>Copilot</span>
          ${a(s.toggleCopilot)}
        </li>
        <li>
          <span>${e.terminal}</span>
          <span>Command window</span>
          ${a(s.toggleCommandWindow)}
        </li>
        <li>
          <span>${e.flipBack}</span>
          <span>Undo</span>
          ${a(s.undo)}
        </li>
        <li>
          <span>${e.flipForward}</span>
          <span>Redo</span>
          ${a(s.redo)}
        </li>
      </ul>
      <h3>Selected component</h3>
      <ul>
        <li>
          <span>${e.fileCodeAlt}</span>
          <span>Go to source</span>
          ${a(s.goToSource)}
        </li>
        ${t ? i`<li>
              <span>${e.code}</span>
              <span>Go to attach source</span>
              ${a(s.goToAttachSource)}
            </li>` : g}
        <li>
          <span>${e.copy}</span>
          <span>Copy</span>
          ${a(s.copy)}
        </li>
        <li>
          <span>${e.clipboard}</span>
          <span>Paste</span>
          ${a(s.paste)}
        </li>
        <li>
          <span>${e.copyAlt}</span>
          <span>Duplicate</span>
          ${a(s.duplicate)}
        </li>
        <li>
          <span>${e.userUp}</span>
          <span>Select parent</span>
          ${a(s.selectParent)}
        </li>
        <li>
          <span>${e.userLeft}</span>
          <span>Select previous sibling</span>
          ${a(s.selectPreviousSibling)}
        </li>
        <li>
          <span>${e.userRight}</span>
          <span>Select first child / next sibling</span>
          ${a(s.selectNextSibling)}
        </li>
        <li>
          <span>${e.trash}</span>
          <span>Delete</span>
          ${a(s.delete)}
        </li>
        <li>
          <span>${e.zap}</span>
          <span>Quick add from palette</span>
          ${a("<kbd>A ... Z</kbd>")}
        </li>
      </ul>`;
  }
};
d = w([
  f("copilot-shortcuts-panel")
], d);
function a(t) {
  return i`<span class="kbds">${b(t)}</span>`;
}
const x = m({
  header: "Keyboard Shortcuts",
  tag: "copilot-shortcuts-panel",
  width: 400,
  height: 550,
  floatingPosition: {
    top: 50,
    left: 50
  }
}), C = {
  init(t) {
    t.addPanel(x);
  }
};
window.Vaadin.copilot.plugins.push(C);
