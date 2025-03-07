import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-header.component.html'
})
export default class PageHeaderComponent {
  @Input() title: string = '';
  @Input() subtitle: string = '';
  @Input() color: 'blue' | 'pink' | 'green' | 'indigo' = 'blue';
  
  get colorClasses(): string {
    switch (this.color) {
      case 'pink':
        return 'from-pink-600 to-pink-800';
      case 'green':
        return 'from-green-600 to-green-800';
      case 'indigo':
        return 'from-indigo-600 to-indigo-800';
      default: // blue
        return 'from-blue-700 to-blue-900';
    }
  }
}
