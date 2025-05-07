import { Component } from '@angular/core';
import { RouterModule } from '@angular/router'; // Necessário se você tiver <router-outlet> no template
import { ProsemirrorEditorComponent } from './prosemirror-editor/prosemirror-editor.component'; // Importa o editor

@Component({
  selector: 'app-root', // Geralmente o seletor do componente raiz
  standalone: true,
  imports: [
    RouterModule, // Adicione se usar <router-outlet>
    ProsemirrorEditorComponent // Importa o componente do editor para usá-lo no template
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Tessera Acadêmica'; // Você pode mudar o título aqui

// Dentro da classe AppComponent em app.component.ts
getAnoAtual(): number {
  return new Date().getFullYear();
}
}