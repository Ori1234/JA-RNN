#בס"ד
#need to download the webdriver that match the chrome version
#גרסה 76.0.3809.132 (גירסה רשמית) (64 סיביות)
#https://sites.google.com/a/chromium.org/chromedriver/downloads
#לייבא

#THE BROWSER SHOULD BE FULL SCREEN!! OR BOTON FOR NEXT PAGE IS HIDDEN BEHIND OTHER ELEMENTS AND DON"T WORK

import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import StaleElementReferenceException 

  
def click(btn_id):
   print('clicking '+ btn_id)
   btn=driver.find_element(By.ID,btn_id)
   webdriver.ActionChains(driver).move_to_element(btn).click(btn).perform()


def wait(id):
   timeout = 30
   try:
       WebDriverWait(driver, timeout).until(EC.visibility_of_element_located((By.ID,id)))
   except TimeoutException:
       driver.quit()

#https://blog.codeship.com/get-selenium-to-wait-for-page-load/ THIS

#output
file1 = open("ONE_BOOK_SELENIUM.txt","wb")
#לגלוש
#FROM USERS/OWNER hi:
driver = webdriver.Chrome(executable_path='chromedriver')
link="https://fjms.genizah.org/"
driver.get(link)

#המשתמש נכנס לפורטל ופותח את הספר המבוקש בכפתור עיון (ידני)
input("Go to jodeo arab portal, go into desired book (ekuzari)and Press Enter to continue...")


'''
#להשיג רשימת מחברים
click("RsrcBtn")
click("ctl00_menuRes1_cmbAuthors_Arrow") #פתיחת #רשימת מחברים
id="ctl00_menuRes1_cmbAuthors_DropDown"
wait(id)
time.sleep(1)
all_authors=driver.find_element(By.ID,id)
time.sleep(1)
authors_list=[]
authors = all_authors.find_elements(By.CLASS_NAME,"rcbItem")
for i in range(len(authors)):
    authors_list.append(authors[i].text)
    #file1.write(b"element in list "+authors[i].text.encode('utf-8')+b'\n')
    print("element in authors ",authors[i].text)


#לולאה לכל מחבר במחברים
first=True
for aut in authors_list:
   id="ctl00_menuRes1_cmbAuthors_Input"
   
   if not first:
      click("RsrcBtn")
      click("ctl00_menuRes1_cmbAuthors_Arrow")
      wait(id)
   else:
      first=False
   choose_author=driver.find_element(By.ID,id)
   choose_author.send_keys(Keys.CONTROL + "a")
   choose_author.send_keys(Keys.DELETE)
   choose_author.send_keys(aut)
   choose_author.send_keys(Keys.ENTER)


    #לגלוש ולהשיג רשימת ספרים
   id="ctl00_menuRes1_cmbTitles_DropDown"      
   wait(id)
   all_books=driver.find_element(By.ID,id)
    
   time.sleep(1)
   books_list=[]
   books = all_books.find_elements(By.CLASS_NAME,"rcbItem")
   for i in range(len(books)):
      books_list.append(books[i].text)
      print("element in books ",books[i].text)

    #לולאה לכל ספר ברשימת ספרים להכנס
   first_book=True
   for bk in books_list:
      file1.write(b'\n\n')
      file1.write(b"###AUTHOR NAME:"+aut.encode("utf-8"))
      file1.write(b'\n')
      file1.write(b"###BOOK NAME:"+bk.encode("utf-8"))
      file1.write(b'\n\n')                 
      if not first_book:
         click("RsrcBtn")
         id="ctl00_menuRes1_cmbAuthors_Input" #needed?
         click("ctl00_menuRes1_cmbAuthors_Arrow")
         wait(id)
         choose_author=driver.find_element(By.ID,id)
         choose_author.send_keys(Keys.CONTROL + "a")
         choose_author.send_keys(Keys.DELETE)
         choose_author.send_keys(aut)
         choose_author.send_keys(Keys.ENTER)
         #need timeout?
      else:
         first_book=False
      id="ctl00_menuRes1_cmbTitles_Input"
      wait(id)
      choose_book=driver.find_element(By.ID,id)
      choose_book.send_keys(Keys.CONTROL + "a")
      choose_book.send_keys(Keys.DELETE)
      choose_book.send_keys(bk)
      choose_book.send_keys(Keys.ENTER)

      #הצג
      click('ctl00_menuRes1_btnViewTitleCont')     
      timeout = 30
      try:
          WebDriverWait(driver, timeout).until(EC.visibility_of_element_located((By.ID,'mode1')))
      except TimeoutException:
          driver.quit()
 '''     
  
  #להשיג טקסט בלולאה כל עוד יש עוד עמודים
end_book=False
page_counter=0
while(not end_book and page_counter<100000000):
	file1.write(b'####page num: '+str(page_counter).encode()+b'\n')
	#make sure that the text is visible
	driver.find_element(By.ID,'mode1').click()
	#wait(id) this is not helpful since it is from before
	time.sleep(3) #needed?
	id='Contentlines'        
	this_text=driver.find_element(By.ID,id)
	#file1.write(this_text.get_attribute('innerHTML').encode("utf-8"))

	all_lines=this_text.find_elements(By.XPATH, './/span[@class=""]')
	
	
	for i in range(len(all_lines)):
		#print("element in list ",all_lines[i].text)
		#file1.write(all_lines[i].text.encode("utf-8"))            
		file1.write(all_lines[i].get_attribute('innerHTML').encode("utf-8"))
		file1.write(b'\n')
  
	file1.flush()

	#עמוד הבא
	btn_id='btnNext'
	btn=driver.find_element(By.ID,btn_id)
	last_page=btn.get_attribute('disabled') #true
	if last_page is not None:
	   print("reached last page")
	   end_book=True
	else:             
	   click('btnNext')
	   page_counter+=1
file1.close()


####TODO: 
#1)remove like this from all_lines[i]: <label class="unitHeader"><span class="hebTxt">תוכן הפרקים</span></label>
#2) remove the pattern : <some letters...> this is probably versions
#3) what to do with the hebrew ? 
#for instance: <span class="hebTxt">מר' ור' אברהם בן הרב מר' ור'</span>
#maybe just str search?
#finally i need 2 lists:
#words and binary labels(heb or arab)
#same length...
#including ponctuations

#for 1) replace all ocurences of : <span><label class="unitHeader">.*</label></span>
#with what to replace? with emtpy string
#also replace string ####page num: \d*\n with empty string
#and <span class="hebTxt"> replace with </span> 
#all using regular expression bulleting in the replace dialog
#for the arab text from bashir- to get rid of the footnotes. opened with google docs. cntl+c on text body, and paste in notepad++ 
