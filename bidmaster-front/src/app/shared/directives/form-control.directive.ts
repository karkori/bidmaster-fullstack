import { Directive, ElementRef, Input, OnInit, Renderer2, HostListener } from '@angular/core';

@Directive({
  selector: '[appFormControl]',
  standalone: true
})
export class FormControlDirective implements OnInit {
  @Input() controlSize: 'sm' | 'md' | 'lg' = 'md';
  @Input() controlStatus: 'default' | 'invalid' | 'valid' | 'disabled' = 'default';
  
  private readonly baseClasses = 'block w-full rounded-md border transition-all duration-200';
  private readonly focusClasses = 'ring-2 ring-offset-0';
  
  constructor(private el: ElementRef, private renderer: Renderer2) {}
  
  ngOnInit(): void {
    this.applyStyles();
  }
  
  @HostListener('focus')
  onFocus(): void {
    // Filtrar elementos vacíos que puedan aparecer después del split
    const focusClasses = this.focusClasses.split(' ').filter(Boolean);
    const colorClasses = this.getFocusColorClasses().split(' ').filter(Boolean);
    
    // Añadir clases solo si hay elementos en el array
    if (focusClasses.length > 0) {
      this.el.nativeElement.classList.add(...focusClasses);
    }
    
    if (colorClasses.length > 0) {
      this.el.nativeElement.classList.add(...colorClasses);
    }
  }
  
  @HostListener('blur')
  onBlur(): void {
    // Filtrar elementos vacíos que puedan aparecer después del split
    const focusClasses = this.focusClasses.split(' ').filter(Boolean);
    const colorClasses = this.getFocusColorClasses().split(' ').filter(Boolean);
    
    // Eliminar clases solo si hay elementos en el array
    if (focusClasses.length > 0) {
      this.el.nativeElement.classList.remove(...focusClasses);
    }
    
    if (colorClasses.length > 0) {
      this.el.nativeElement.classList.remove(...colorClasses);
    }
  }
  
  @HostListener('input', ['$event'])
  onInput(event: any): void {
    const isValid = this.el.nativeElement.validity.valid;
    const isEmpty = !this.el.nativeElement.value;
    
    if (isEmpty) {
      this.controlStatus = 'default';
    } else {
      this.controlStatus = isValid ? 'valid' : 'invalid';
    }
    
    this.applyStyles();
  }
  
  private applyStyles(): void {
    // Eliminar clases antiguas
    const classes = this.el.nativeElement.className.split(' ');
    for (const cls of classes) {
      if (cls.includes('border-') || cls.includes('shadow-') || cls.includes('focus:')) {
        this.renderer.removeClass(this.el.nativeElement, cls);
      }
    }
    
    // Aplicar clases base
    const baseClasses = this.baseClasses.split(' ');
    for (const cls of baseClasses) {
      this.renderer.addClass(this.el.nativeElement, cls);
    }
    
    // Aplicar clases específicas según tamaño
    const sizeClasses = this.getSizeClasses();
    const sizeClassesArray = sizeClasses.split(' ');
    for (const cls of sizeClassesArray) {
      this.renderer.addClass(this.el.nativeElement, cls);
    }
    
    // Aplicar clases específicas según estado
    const statusClasses = this.getStatusClasses();
    const statusClassesArray = statusClasses.split(' ');
    for (const cls of statusClassesArray) {
      this.renderer.addClass(this.el.nativeElement, cls);
    }
  }
  
  private getSizeClasses(): string {
    switch (this.controlSize) {
      case 'sm':
        return 'px-2 py-1 text-sm';
      case 'lg':
        return 'px-4 py-3 text-lg';
      default: // md
        return 'px-3 py-2 text-base';
    }
  }
  
  private getStatusClasses(): string {
    switch (this.controlStatus) {
      case 'invalid':
        return 'border-red-500 shadow-sm focus:border-red-500 focus:shadow-sm';
      case 'valid':
        return 'border-green-500 shadow-sm focus:border-green-500 focus:shadow-sm';
      case 'disabled':
        return 'border-gray-300 bg-gray-100 text-gray-500 cursor-not-allowed shadow-none';
      default:
        return 'border-gray-300 shadow-sm hover:border-gray-400';
    }
  }
  
  private getFocusColorClasses(): string {
    switch (this.controlStatus) {
      case 'invalid':
        return 'ring-red-200';
      case 'valid':
        return 'ring-green-200';
      case 'disabled':
        return '';
      default:
        return 'ring-blue-200';
    }
  }
}
