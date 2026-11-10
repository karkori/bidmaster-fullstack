import { Directive, ElementRef, OnInit, Renderer2, inject, input } from '@angular/core';

@Directive({
  selector: '[appFormLabel]',
  standalone: true
})
export class FormLabelDirective implements OnInit {
  required = input<boolean>(false);
  size = input<'sm' | 'md' | 'lg'>('md');
  
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);
  
  ngOnInit(): void {
    // Aplicar clases base
    const baseClasses = 'block font-medium text-gray-700 mb-1'.split(' ');
    for (const cls of baseClasses) {
      this.renderer.addClass(this.el.nativeElement, cls);
    }
    
    // Aplicar clases según tamaño
    const sizeClass = this.getSizeClass();
    this.renderer.addClass(this.el.nativeElement, sizeClass);
    
    // Añadir indicador de requerido si es necesario
    if (this.required()) {
      const requiredSpan = this.renderer.createElement('span');
      const text = this.renderer.createText(' *');
      this.renderer.addClass(requiredSpan, 'text-red-500');
      this.renderer.appendChild(requiredSpan, text);
      this.renderer.appendChild(this.el.nativeElement, requiredSpan);
    }
  }
  
  private getSizeClass(): string {
    switch (this.size()) {
      case 'sm':
        return 'text-xs';
      case 'lg':
        return 'text-base';
      default: // md
        return 'text-sm';
    }
  }
}
