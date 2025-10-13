import '@vaadin/radio-group/vaadin-radio-group.js';
import '@vaadin/radio-group/vaadin-radio-button.js';
import { LitElement, html, css } from 'lit';
import { property, customElement } from 'lit/decorators.js';
import { SignalWatcher } from '@lit-labs/preact-signals';
import { theme, sharedStyles, onChange } from './theme-select.js';

@customElement('theme-radio-group')
export class ThemeRadioGroup extends SignalWatcher(LitElement) {
  createRenderRoot() {
    return this;
  }

  @property()
  label?: string;

  render() {
    return html`
      <vaadin-radio-group
        class="theme-select"
        value=${theme.value}
        @change=${onChange}
        .label=${this.label}
      >
        <vaadin-radio-button value="system">
          <label slot="label">
            <span class="theme-icon-system"></span>
            <span class="theme-label-system"></span>
          </label>
        </vaadin-radio-button>
        <vaadin-radio-button value="light">
          <label slot="label">
            <span class="theme-icon-light"></span>
            <span class="theme-label-light"></span>
          </label>
        </vaadin-radio-button>
        <vaadin-radio-button value="dark">
          <label slot="label">
            <span class="theme-icon-dark"></span>
            <span class="theme-label-dark"></span>
          </label>
        </vaadin-radio-button>
      </vaadin-radio-group>
    `;
  }
};

const styles = document.createElement('style');
styles.textContent = css`
  ${sharedStyles}

  theme-radio-group {
    display: contents;
    font-weight: 500;
  }

  theme-radio-group vaadin-radio-group {
    display: flex;
    font: inherit;
  }

  theme-radio-group vaadin-radio-group::part(group-field) {
    flex-wrap: nowrap;
    gap: var(--lumo-space-s, 8px);
  }

  theme-radio-group vaadin-radio-button {
    flex: 1;
    font: inherit;
  }

  theme-radio-group vaadin-radio-button::part(radio) {
    display: none;
  }

  theme-radio-group vaadin-radio-button label {
    height: var(--lumo-size-m);
    padding: 0 var(--lumo-space-m, 16px);
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--lumo-space-s);
    border: 1px solid var(--lumo-contrast-20pct, #ddd);
    border-radius: var(--lumo-border-radius-m, 4px);
  }

  theme-radio-group vaadin-radio-button[checked] label {
    border-color: var(--lumo-primary-color);
    box-shadow: inset 0 0 0 1px var(--lumo-primary-color);
  }

  @media (any-hover: hover) {
    theme-radio-group vaadin-radio-button:hover label {
      background-color: var(--lumo-contrast-5pct, #eee);
    }
  }

  theme-radio-group vaadin-radio-button[focus-ring] label {
    outline: var(--vaadin-focus-ring-width, 2px) solid var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
    outline-offset: 2px;
  }
`.toString();
document.head.append(styles);
