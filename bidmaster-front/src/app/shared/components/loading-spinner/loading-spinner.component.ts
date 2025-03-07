import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loading-spinner.component.html'
})
export default class LoadingSpinnerComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() color: 'blue' | 'pink' | 'green' | 'indigo' = 'blue';
  @Input() message: string = 'Cargando...';
  
  get sizeClasses(): string {
    switch (this.size) {
      case 'sm':
        return 'h-6 w-6';
      case 'lg':
        return 'h-12 w-12';
      default: // md
        return 'h-8 w-8';
    }
  }
  
  get colorClasses(): string {
    switch (this.color) {
      case 'pink':
        return 'border-pink-600';
      case 'green':
        return 'border-green-600';
      case 'indigo':
        return 'border-indigo-600';
      default: // blue
        return 'border-blue-600';
    }
  }
}
