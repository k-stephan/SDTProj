require 'au3'

def close_popup(title) 
  begin    
    monitor = AutoItX3::Window.new(title)
    return false unless monitor.wait(3)
    monitor.activate   
    AutoItX3.send_keys("username")
    sleep 1
    AutoItX3.send_keys("{TAB}password")
    sleep 1
    AutoItX3.send_keys("{TAB}{ENTER}")
    sleep 1
    return true
  rescue   
  end 
  return false
end

##################
# Main
#   ie -> close_popup("Windows Security")
#   chrome  -> close_popup("")
##################
begin
  # Firefox
  exit 0 if close_popup("Windows Security")    
  exit 1
end
