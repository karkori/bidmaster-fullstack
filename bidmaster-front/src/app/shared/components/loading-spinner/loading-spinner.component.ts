import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [],
  templateUrl: './loading-spinner.component.html'
})
export default class LoadingSpinnerComponent {
  size = input<'sm' | 'md' | 'lg'>('md');
  color = input<'blue' | 'pink' | 'green' | 'indigo'>('blue');
  message = input<string>('Cargando...');
  
  sizeClasses = computed(() => {
    switch (this.size()) {
      case 'sm':
        return 'h-6 w-6';
      case 'lg':
        return 'h-12 w-12';
      default: // md
        return 'h-8 w-8';
    }
  });
  
  colorClasses = computed(() => {
    switch (this.color()) {
      case 'pink':
        return 'border-pink-600';
      case 'green':
        return 'border-green-600';
      case 'indigo':
        return 'border-indigo-600';
      default: // blue
        return 'border-blue-600';
    }
  });
}
