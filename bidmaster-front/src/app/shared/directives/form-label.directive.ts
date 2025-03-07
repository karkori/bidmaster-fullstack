import { Directive, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appFormLabel]',
  standalone: true
})
export class FormLabelDirective implements OnInit {
  @Input() required = false;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  
  constructor(private el: ElementRef, private renderer: Renderer2) {}
  
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
    if (this.required) {
      const requiredSpan = this.renderer.createElement('span');
      const text = this.renderer.createText(' *');
      this.renderer.addClass(requiredSpan, 'text-red-500');
      this.renderer.appendChild(requiredSpan, text);
      this.renderer.appendChild(this.el.nativeElement, requiredSpan);
    }
  }
  
  private getSizeClass(): string {
    switch (this.size) {
      case 'sm':
        return 'text-xs';
      case 'lg':
        return 'text-base';
      default: // md
        return 'text-sm';
    }
  }
}
