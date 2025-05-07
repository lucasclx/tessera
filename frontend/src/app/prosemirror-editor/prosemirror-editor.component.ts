import { Component, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild, ChangeDetectorRef } from '@angular/core';

// Importações do ProseMirror
import { EditorState, Transaction } from 'prosemirror-state';
import { EditorView } from 'prosemirror-view';
import { Schema, DOMParser, DOMSerializer, Node as ProseMirrorNode, MarkType, NodeType } from 'prosemirror-model';
import { schema as basicSchema } from 'prosemirror-schema-basic';
import { addListNodes } from 'prosemirror-schema-list';
import { exampleSetup } from 'prosemirror-example-setup';

// Comandos específicos
import { toggleMark, setBlockType as pmSetBlockType } from 'prosemirror-commands';
import { wrapInList as pmWrapInList } from 'prosemirror-schema-list';

const LOCAL_STORAGE_KEY = 'tesseraDocumentContent';

@Component({
  selector: 'app-prosemirror-editor',
  standalone: true,
  imports: [
    // CommonModule // Adicione se usar diretivas como *ngIf, *ngFor no template deste componente
  ],
  templateUrl: './prosemirror-editor.component.html',
  styleUrls: ['./prosemirror-editor.component.scss']
})
export class ProsemirrorEditorComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('editorRef', { static: true }) editorRef!: ElementRef<HTMLDivElement>;

  public view!: EditorView;
  private prosemirrorSchema!: Schema;

  // Propriedades para o estado ativo dos botões da barra de ferramentas
  isBoldActive = false;
  isItalicActive = false;
  isParagraphActive = false;
  activeHeadingLevel: number | null = null;
  isBulletListActive = false;
  isOrderedListActive = false;

  constructor(private cdRef: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.prosemirrorSchema = new Schema({
      nodes: addListNodes(basicSchema.spec.nodes, "paragraph block*", "block"),
      marks: basicSchema.spec.marks
    });
  }

  ngAfterViewInit(): void {
    if (this.editorRef && this.editorRef.nativeElement) {
      const plugins = exampleSetup({
        schema: this.prosemirrorSchema,
        menuBar: false,
      });

      // --- INÍCIO DA MODIFICAÇÃO PARA TESTE ---
      // Forçar um conteúdo inicial simples, ignorando o localStorage por enquanto
      let initialState: EditorState;
      try {
        const defaultDocNode = this.prosemirrorSchema.node('paragraph', null, [
          this.prosemirrorSchema.text('Editor visível? Digite aqui!')
        ]);
        initialState = EditorState.create({
          doc: defaultDocNode,
          plugins,
          schema: this.prosemirrorSchema
        });
        console.log("TESTE: Iniciando com conteúdo padrão forçado.");
      } catch(error) {
        console.error("TESTE: Erro ao criar estado com conteúdo padrão forçado:", error);
        initialState = EditorState.create({ schema: this.prosemirrorSchema, plugins });
      }
      // --- FIM DA MODIFICAÇÃO PARA TESTE ---

      /*
      // LÓGICA ORIGINAL DE CARREGAMENTO DO LOCALSTORAGE (TEMPORARIAMENTE COMENTADA)
      const savedContent = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (savedContent) {
        try {
          const jsonContent = JSON.parse(savedContent);
          const doc = ProseMirrorNode.fromJSON(this.prosemirrorSchema, jsonContent);
          initialState = EditorState.create({ doc, plugins, schema: this.prosemirrorSchema }); // Use the loaded doc
          console.log("Conteúdo carregado do localStorage!");
        } catch (error) {
          console.error("Erro ao carregar conteúdo do localStorage:", error);
          initialState = EditorState.create({ schema: this.prosemirrorSchema, plugins }); // Fallback
        }
      } else {
        initialState = EditorState.create({ schema: this.prosemirrorSchema, plugins }); // Empty state
      }
      */

      this.view = new EditorView(this.editorRef.nativeElement, {
        state: initialState,
        dispatchTransaction: (transaction: Transaction) => {
          const newState = this.view.state.apply(transaction);
          this.view.updateState(newState);
          this.updateButtonActiveStates(newState);
        }
      });
      this.updateButtonActiveStates(this.view.state);
      this.view.focus();
      console.log("Editor ProseMirror com toolbar customizada e feedback visual inicializado!");
    } else {
      console.error("Elemento de referência do editor não encontrado!");
    }
  }

  ngOnDestroy(): void {
    if (this.view) {
      this.view.destroy();
    }
  }

  private updateButtonActiveStates(state: EditorState): void {
    const { selection } = state;
    const { $from, $to, empty } = selection;

    const strongMark = this.prosemirrorSchema.marks['strong'] as MarkType;
    const emMark = this.prosemirrorSchema.marks['em'] as MarkType;

    if (empty) {
      this.isBoldActive = !!strongMark.isInSet(state.storedMarks || $from.marks());
      this.isItalicActive = !!emMark.isInSet(state.storedMarks || $from.marks());
    } else {
      this.isBoldActive = state.doc.rangeHasMark($from.pos, $to.pos, strongMark);
      this.isItalicActive = state.doc.rangeHasMark($from.pos, $to.pos, emMark);
    }

    const parentNode = $from.parent;
    const paragraphNode = this.prosemirrorSchema.nodes['paragraph'] as NodeType;
    const headingNode = this.prosemirrorSchema.nodes['heading'] as NodeType;
    const bulletListNode = this.prosemirrorSchema.nodes['bullet_list'] as NodeType;
    const orderedListNode = this.prosemirrorSchema.nodes['ordered_list'] as NodeType;

    this.isParagraphActive = parentNode.type === paragraphNode;
    if (parentNode.type === headingNode) {
      this.activeHeadingLevel = parentNode.attrs['level'] as number;
    } else {
      this.activeHeadingLevel = null;
    }

    let listActive = false;
    let orderedListActive = false;
    for (let i = $from.depth - 1; i > 0; i--) {
        const ancestor = $from.node(i);
        if (ancestor.type === bulletListNode) { listActive = true; break; }
        if (ancestor.type === orderedListNode) { orderedListActive = true; break; }
    }
    this.isBulletListActive = listActive;
    this.isOrderedListActive = orderedListActive;

    this.cdRef.detectChanges();
  }

  private applyCommand(command: (state: EditorState, dispatch?: (tr: Transaction) => void) => boolean): void {
    if (this.view) {
      this.view.focus();
      command(this.view.state, this.view.dispatch);
    }
  }

  toggleBold(): void { this.applyCommand(toggleMark(this.prosemirrorSchema.marks['strong'] as MarkType)); }
  toggleItalic(): void { this.applyCommand(toggleMark(this.prosemirrorSchema.marks['em'] as MarkType)); }
  setParagraph(): void { this.applyCommand(pmSetBlockType(this.prosemirrorSchema.nodes['paragraph'] as NodeType)); }
  setHeading(level: number): void { this.applyCommand(pmSetBlockType(this.prosemirrorSchema.nodes['heading'] as NodeType, { level })); }
  wrapInBulletList(): void { this.applyCommand(pmWrapInList(this.prosemirrorSchema.nodes['bullet_list'] as NodeType)); }
  wrapInOrderedList(): void { this.applyCommand(pmWrapInList(this.prosemirrorSchema.nodes['ordered_list'] as NodeType, {})); }

  saveContent(): void {
    if (this.view) {
      const contentJSON = this.view.state.doc.toJSON();
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(contentJSON));
      console.log("Conteúdo salvo no localStorage!", contentJSON);
      alert("Conteúdo salvo localmente!");
    }
  }

  loadContent(): void {
    if (this.view) {
      const savedContent = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (savedContent) {
        try {
          const jsonContent = JSON.parse(savedContent);
          const newDocNode = ProseMirrorNode.fromJSON(this.prosemirrorSchema, jsonContent);
          const newState = EditorState.create({
            doc: newDocNode,
            plugins: this.view.state.plugins,
            schema: this.prosemirrorSchema
          });
          this.view.updateState(newState);
          this.updateButtonActiveStates(newState);
          console.log("Conteúdo carregado do localStorage para o editor!");
          alert("Conteúdo carregado!");
        } catch (error) {
          console.error("Erro ao carregar ou parsear conteúdo do localStorage:", error);
          alert("Erro ao carregar conteúdo.");
        }
      } else {
        alert("Nenhum conteúdo salvo encontrado localmente.");
      }
    }
  }

  getEditorContentJSON(): any { return this.view?.state.doc.toJSON(); }
  getEditorContentHTML(): string {
    if (!this.view || !this.prosemirrorSchema) return '';
    const fragment = DOMSerializer.fromSchema(this.prosemirrorSchema).serializeFragment(this.view.state.doc.content);
    const div = document.createElement('div'); div.appendChild(fragment); return div.innerHTML;
  }
}