import '@vaadin/select/vaadin-lit-select';
import { LitElement, html, css } from 'lit';
import { property, customElement } from 'lit/decorators.js';
import { ifDefined } from 'lit/directives/if-defined.js';
import { SignalWatcher, signal } from '@lit-labs/preact-signals';

export const theme = signal(localStorage.getItem('theme') ?? 'system');

export const onChange = (e: any) => {
  const selectedTheme = e.target.value;

  if (selectedTheme != 'system') {
    localStorage.setItem('theme', selectedTheme);
  } else {
    localStorage.removeItem('theme');
  }

  theme.value = selectedTheme;

  // @ts-ignore
  window._themeSelect.updateThemeAttribute();
}

@customElement('theme-select')
export class ThemeSelect extends SignalWatcher(LitElement) {
  createRenderRoot() {
    return this;
  }

  @property()
  label?: string;

  render() {
    return html`
      <vaadin-select
        class="theme-select"
        overlay-class="theme-select"
        .label=${this.label}
        aria-label=${ifDefined(this.label)}
        .value=${theme.value}
        .renderer=${(root: Element) => {
          const listBox = document.createElement('vaadin-list-box');
          ['system', 'light' , 'dark'].forEach((value) => {
            const item = document.createElement('vaadin-select-item');
            item.innerHTML = `<span class="theme-icon-${value}"></span><span class="theme-label-${value}"><span>`;
            item.setAttribute('value', value);
            listBox.appendChild(item);
          });

          root.innerHTML = '';
          root.appendChild(listBox);
        }}
        @change=${onChange}>
      </vaadin-select>
    `;
  }
};

export const sharedStyles = css`
  .theme-select .theme-label-system::before {
    content: var(--theme-label-system, "System");
  }

  .theme-select .theme-label-light::before {
    content: var(--theme-label-light, "Light");
  }

  .theme-select .theme-label-dark::before {
    content: var(--theme-label-dark, "Dark");
  }

  .theme-select [class^=theme-icon] {
    display: inline-block;
    width: var(--lumo-icon-size-s, 16px);
    height: var(--lumo-icon-size-s, 16px);
    background: currentColor;
  }

  .theme-select .theme-icon-system {
    mask-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 17.25v1.007a3 3 0 0 1-.879 2.122L7.5 21h9l-.621-.621A3 3 0 0 1 15 18.257V17.25m6-12V15a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 15V5.25m18 0A2.25 2.25 0 0 0 18.75 3H5.25A2.25 2.25 0 0 0 3 5.25m18 0V12a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 12V5.25" /></svg>');
  }

  :is(html:not([theme]), html[theme~=light]) theme-select.minimal .theme-icon-system,
  .theme-select .theme-icon-light {
    mask-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 3v2.25m6.364.386-1.591 1.591M21 12h-2.25m-.386 6.364-1.591-1.591M12 18.75V21m-4.773-4.227-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0Z" /></svg>');
  }

  html[theme~=dark] theme-select.minimal .theme-icon-system,
  .theme-select .theme-icon-dark {
    mask-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M21.752 15.002A9.72 9.72 0 0 1 18 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 0 0 3 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 0 0 9.002-5.998Z" /></svg>');
  }
`;

const styles = document.createElement('style');
styles.textContent = css`
  ${sharedStyles}

  theme-select {
    display: contents;
  }

  theme-select vaadin-select {
    --vaadin-field-default-width: auto;
  }

  theme-select.minimal vaadin-select {
    --vaadin-input-field-border-width: 0;
    --vaadin-input-field-background: transparent;
    padding: 0;
  }

  theme-select.minimal vaadin-select::before {
    margin-top: 0;
  }

  theme-select.minimal vaadin-select::part(label) {
    display: none;
  }

  theme-select.minimal vaadin-select::part(toggle-button) {
    display: none;
  }

  theme-select vaadin-select-value-button {
    width: auto;
    mask-image: none;
  }

  theme-select.minimal vaadin-select-value-button [class^=theme-label] {
    display: none;
  }

  vaadin-select-overlay.theme-select vaadin-select-item {
    padding: 0 var(--lumo-space-s, 8px);
  }

  vaadin-select-overlay.theme-select vaadin-select-item::part(checkmark) {
    order: 1;
  }

  .theme-select vaadin-select-item::part(content) {
    display: flex;
    align-items: center;
    gap: var(--lumo-space-s, 8px);
  }

  @media (any-hover: hover) {
    theme-select.minimal vaadin-select::part(input-field):hover {
      background-color: var(--lumo-contrast-5pct, #efefef);
    }
  }

  vaadin-select-overlay.theme-select .theme-icon-system {
    mask-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 17.25v1.007a3 3 0 0 1-.879 2.122L7.5 21h9l-.621-.621A3 3 0 0 1 15 18.257V17.25m6-12V15a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 15V5.25m18 0A2.25 2.25 0 0 0 18.75 3H5.25A2.25 2.25 0 0 0 3 5.25m18 0V12a2.25 2.25 0 0 1-2.25 2.25H5.25A2.25 2.25 0 0 1 3 12V5.25" /></svg>');
  }
`.toString();
document.head.append(styles);
