import { PolymerElement, html } from '@polymer/polymer/polymer-element.js';
import { GridTreeToggle } from '@vaadin/grid/src/vaadin-grid-tree-toggle.js';

export class CustomGridTreeToggle extends GridTreeToggle  {
  static get is() {
    return 'custom-grid-tree-toggle'
  }

  ready() {
	  super.ready();
	  const cell = this.parentElement.getAttribute('slot').split('-')[4];
	  const td = this.parentElement.parentElement.shadowRoot.querySelector('#vaadin-grid-cell-'+cell);
      td.addEventListener('keyup', (e) => this._onKeyDown(e));
  }

  _onKeyDown(e) {
	  if (e.keyCode == 32) {
		  e.stopPropagation();
		  this.dispatchEvent(new CustomEvent('click'));
	  }
	  if (e.keyCode == 13) {
		  e.stopPropagation();
		  this.dispatchEvent(new CustomEvent('doselect'));
	  }
  }

  static get template () {
    return html`
        ${super.template}
      `;
  }
}

customElements.define('custom-grid-tree-toggle', CustomGridTreeToggle);