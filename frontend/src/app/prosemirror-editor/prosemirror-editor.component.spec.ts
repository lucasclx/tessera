import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProsemirrorEditorComponent } from './prosemirror-editor.component';

describe('ProsemirrorEditorComponent', () => {
  let component: ProsemirrorEditorComponent;
  let fixture: ComponentFixture<ProsemirrorEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProsemirrorEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProsemirrorEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
