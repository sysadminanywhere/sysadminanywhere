import { F as p, b as d, w as u, a4 as v, s as f, P as b, H as m, u as g } from "./copilot-CH8EJMw_.js";
import { r as c } from "./state-pxIM5gDR.js";
import { m as y, e as k } from "./overlay-monkeypatch-D1O-dOi0.js";
import { B as w } from "./base-panel-B2een6M0.js";
import { i as x } from "./icons-Ca9sB-3j.js";
const $ = "copilot-feedback-panel{display:flex;flex-direction:column;font:var(--font-xsmall);gap:var(--space-200);padding:var(--space-150)}copilot-feedback-panel>p{margin:0}copilot-feedback-panel .dialog-footer{display:flex;gap:var(--space-100)}copilot-feedback-panel :is(vaadin-select,vaadin-text-area,vaadin-text-field){padding:0}copilot-feedback-panel :is(vaadin-select,vaadin-text-area,vaadin-text-field)::part(input-field),copilot-feedback-panel vaadin-select-value-button{padding:0}copilot-feedback-panel vaadin-select::part(toggle-button){align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}copilot-feedback-panel vaadin-text-area textarea{line-height:var(--line-height-1);padding:calc((var(--size-m) - var(--line-height-1)) / 2) var(--space-100)}copilot-feedback-panel vaadin-text-area:hover::part(input-field){background:none}copilot-feedback-panel vaadin-text-field input{padding:0 var(--space-100)}copilot-feedback-panel>*::part(label){font-weight:var(--font-weight-medium);line-height:var(--line-height-1);margin:0;padding:0 var(--space-150) var(--space-50) 0}copilot-feedback-panel>*::part(helper-text){line-height:var(--line-height-1);margin:0}";
var A = Object.defineProperty, P = Object.getOwnPropertyDescriptor, o = (e, t, n, s) => {
  for (var i = s > 1 ? void 0 : s ? P(t, n) : t, l = e.length - 1, r; l >= 0; l--)
    (r = e[l]) && (i = (s ? r(t, n, i) : r(i)) || i);
  return s && i && A(t, n, i), i;
};
const h = "https://github.com/vaadin/copilot/issues/new", F = "?template=feature_request.md&title=%5BFEATURE%5D", T = "A short, concise description of the bug and why you consider it a bug. Any details like exceptions and logs can be helpful as well.", D = "Please provide as many details as possible, this will help us deliver a fix as soon as possible.%0AThank you!%0A%0A%23%23%23 Description of the Bug%0A%0A{description}%0A%0A%23%23%23 Expected Behavior%0A%0AA description of what you would expect to happen. (Sometimes it is clear what the expected outcome is if something does not work, other times, it is not super clear.)%0A%0A%23%23%23 Minimal Reproducible Example%0A%0AWe would appreciate the minimum code with which we can reproduce the issue.%0A%0A%23%23%23 Versions%0A{versionsInfo}";
let a = class extends w {
  constructor() {
    super(), this.description = "", this.items = [
      {
        label: "Report a Bug",
        value: "bug",
        ghTitle: "[BUG]"
      },
      {
        label: "Ask a Question",
        value: "question",
        ghTitle: "[QUESTION]"
      },
      {
        label: "Share an Idea",
        value: "idea",
        ghTitle: "[FEATURE]"
      }
    ];
  }
  render() {
    return p`<style>
        ${$}</style
      >${this.renderContent()}${this.renderFooter()}`;
  }
  firstUpdated() {
    y(this);
  }
  renderContent() {
    return this.message === void 0 ? p`
          <p>
            Your insights are incredibly valuable to us. Whether you’ve encountered a hiccup, have questions, or ideas
            to make our platform better, we're all ears! If you wish, leave your email and we’ll get back to you. You
            can even share your code snippet with us for a clearer picture.
          </p>
          <vaadin-select
            label="What's on Your Mind?"
            .items="${this.items}"
            .value="${this.items[0].value}"
            @value-changed=${(e) => {
      this.type = e.detail.value;
    }}>
          </vaadin-select>
          <vaadin-text-area
            .value="${this.description}"
            @keydown=${this.keyDown}
            @focus=${() => {
      this.descriptionField.invalid = !1, this.descriptionField.placeholder = "";
    }}
            @value-changed=${(e) => {
      this.description = e.detail.value;
    }}
            label="Tell Us More"
            helper-text="Describe what you're experiencing, wondering about, or envisioning. The more you share, the better we can understand and act on your feedback"></vaadin-text-area>
          <vaadin-text-field
            @keydown=${this.keyDown}
            @value-changed=${(e) => {
      this.email = e.detail.value;
    }}
            id="email"
            label="Your Email (Optional)"
            helper-text="Leave your email if you’d like us to follow up. Totally optional, but we’d love to keep the conversation going."></vaadin-text-field>
        ` : p`<p>${this.message}</p>`;
  }
  renderFooter() {
    return this.message === void 0 ? p`
          <div class="dialog-footer">
            <button
              style="margin-inline-end: auto"
              @click="${() => d.emit("system-info-with-callback", {
      callback: (e) => this.openGithub(e, this),
      notify: !1
    })}">
              <span class="prefix">${x.github}</span>
              Create GitHub Issue
            </button>
            <button @click="${this.close}">Cancel</button>
            <button class="primary" @click="${this.submit}">Submit</button>
          </div>
        ` : p` <div class="footer">
          <vaadin-button @click="${this.close}">Close</vaadin-button>
        </div>`;
  }
  close() {
    u.updatePanel("copilot-feedback-panel", {
      floating: !1
    });
  }
  submit() {
    if (v("feedback"), this.description.trim() === "") {
      this.descriptionField.invalid = !0, this.descriptionField.placeholder = "Please tell us more before sending", this.descriptionField.value = "";
      return;
    }
    const e = {
      description: this.description,
      email: this.email,
      type: this.type
    };
    d.emit("system-info-with-callback", {
      callback: (t) => f(`${b}feedback`, { ...e, versions: t }),
      notify: !1
    }), this.parentNode?.style.setProperty("--section-height", "150px"), this.message = "Thank you for sharing feedback.";
  }
  keyDown(e) {
    (e.key === "Backspace" || e.key === "Delete") && e.stopPropagation();
  }
  openGithub(e, t) {
    if (this.type === "idea") {
      window.open(`${h}${F}`);
      return;
    }
    const n = e.replace(/\n/g, "%0A"), s = `${t.items.find((r) => r.value === this.type)?.ghTitle}`, i = t.description !== "" ? t.description : T, l = D.replace("{description}", i).replace("{versionsInfo}", n);
    window.open(`${h}?title=${s}&body=${l}`, "_blank")?.focus();
  }
};
o([
  c()
], a.prototype, "description", 2);
o([
  c()
], a.prototype, "type", 2);
o([
  c()
], a.prototype, "email", 2);
o([
  c()
], a.prototype, "message", 2);
o([
  c()
], a.prototype, "items", 2);
o([
  k("vaadin-text-area")
], a.prototype, "descriptionField", 2);
a = o([
  g("copilot-feedback-panel")
], a);
const E = m({
  header: "Help Us Improve!",
  tag: "copilot-feedback-panel",
  width: 500,
  height: 500,
  floatingPosition: {
    top: 50,
    left: 50
  }
}), B = {
  init(e) {
    e.addPanel(E);
  }
};
window.Vaadin.copilot.plugins.push(B);
export {
  a as CopilotFeedbackPanel
};
